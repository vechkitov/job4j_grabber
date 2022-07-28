package ru.job4j.grabber;

import java.io.IOException;
import java.util.List;

public interface Parse {

    /**
     * Загружает список всех постов с сайта
     *
     * @param link адерс сайта
     * @return список постов
     * @throws IOException
     */
    List<Post> list(String link) throws IOException;
}
