package com.news.newsback.application.news;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import com.news.newsback.domain.news.repository.NewsRepository;
import com.news.newsback.domain.news.model.NewsSource;
import com.news.newsback.domain.news.service.NewsValidator;
import com.news.newsback.infra.rss.RssParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsGatheringService {

    private final NewsRepository newsRepository;
    private final NewsValidator newsValidator;
    private final RestTemplate restTemplate;
    private final RssParser rssParser;

    @Transactional
    public List<News> gatherNewsFromRss(NewsSource source) {
        String xml;
        try {
            xml = restTemplate.getForObject(source.url(), String.class);
        } catch (Exception e) {
            throw new NewsException(NewsErrorCode.RSS_FETCH_FAILED, e);
        }

        if (xml == null || xml.isEmpty()) {
            throw new NewsException(NewsErrorCode.RSS_FETCH_FAILED);
        }

        List<News> parsedNews = rssParser.parse(xml, source.name(), source.language(), source.region());

        List<News> savedNews = new ArrayList<>();
        for (News news : parsedNews) {
            if (newsValidator.isDuplicate(news)) {
                continue;
            }

            savedNews.add(newsRepository.save(news));
        }

        return savedNews;
    }
}
