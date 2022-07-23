package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.StringJoiner;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK =
            String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            Document document = connection.get();
            for (Element vacancy : document.select(".vacancy-card__inner")) {
                Element titleElement = vacancy.selectFirst(".vacancy-card__title");
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = vacancy.selectFirst(".vacancy-card__date")
                        .child(0)
                        .attr("datetime");
                Post post = new Post(vacancyName,
                        link,
                        retrieveDescription(link),
                        new HabrCareerDateTimeParser().parse(date));
                System.out.println(post);
            }
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        StringJoiner desc = new StringJoiner(System.lineSeparator());
        generateDescription(
                document.selectFirst(".collapsible-description__content").children(), desc);
        return desc.toString();
    }

    private static void generateDescription(Elements elements, StringJoiner desc) {
        for (Element el : elements) {
            if (el.children().size() > 0) {
                generateDescription(el.children(), desc);
            } else {
                desc.add(el.text());
            }
        }
    }
}
