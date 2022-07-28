package ru.job4j.grabber;

import java.util.List;

public interface Parse {

    /**
     * Загружает список всех постов с сайта
     *
     * @param link
     * @return
     */
    List<Post> list(String link);
}
