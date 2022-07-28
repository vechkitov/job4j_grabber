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
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        new HabrCareerParse(new HabrCareerDateTimeParser())
                .list("https://career.habr.com")
                .forEach(System.out::println);
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();
        String pageLink = String.format("%s/vacancies/java_developer?page=", link);
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(pageLink + i);
            Document document = connection.get();
            for (Element vacancy : document.select(".vacancy-card__inner")) {
                Element titleElement = vacancy.selectFirst(".vacancy-card__title");
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
                String date = vacancy.selectFirst(".vacancy-card__date")
                        .child(0)
                        .attr("datetime");
                posts.add(new Post(vacancyName,
                        vacancyLink,
                        retrieveDescription(vacancyLink),
                        dateTimeParser.parse(date)));
            }
        }
        return posts;
    }

    private static String retrieveDescription(String link) {
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
