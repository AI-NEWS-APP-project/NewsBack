package com.news.newsback.infra.rss;

import com.news.newsback.domain.news.model.News;
import java.util.List;

public interface RssParser {
    List<News> parse(String xml, String source, String language, String region);
}
