package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    public static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PAGE_LINK =
            String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    public static final int NUM_OF_PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= NUM_OF_PAGES; i++) {
            String pageLink = String.format("%s%d", link, i);
            try {
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                for (Element vacancy : document.select(".vacancy-card__inner")) {
                    posts.add(parsePostPage(SOURCE_LINK, vacancy));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format(
                        "Не удалось загрузить страницу по адресу: %s", pageLink), e);
            }
        }
        return posts;
    }

    private Post parsePostPage(String link, Element vacancy) {
        Element titleElement = vacancy.selectFirst(".vacancy-card__title");
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
        String date = vacancy.selectFirst(".vacancy-card__date")
                .child(0)
                .attr("datetime");
        return new Post(vacancyName,
                vacancyLink,
                retrieveDescription(vacancyLink),
                dateTimeParser.parse(date));
    }

    private String retrieveDescription(String link) {
        try {
            return Jsoup.connect(link)
                    .get()
                    .selectFirst(".style-ugc")
                    .text();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "Не удалось загрузить страницу по адресу: %s", link), e);
        }
    }
}
