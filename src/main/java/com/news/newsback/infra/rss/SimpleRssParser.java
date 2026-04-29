package com.news.newsback.infra.rss;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
public class SimpleRssParser implements RssParser {

    private static final DateTimeFormatter RSS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    @Override
    public List<News> parse(String xml, String source, String language, String region) {
        List<News> newsList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                
                Optional<String> titleOpt = Optional.ofNullable(getTagValue(item, "title"));
                Optional<String> contentOpt = Optional.ofNullable(getTagValue(item, "description"));
                Optional<String> urlOpt = Optional.ofNullable(getTagValue(item, "link"));

                if (titleOpt.isEmpty() || contentOpt.isEmpty() || urlOpt.isEmpty()) {
                    log.warn("RSS item 필수 필드 누락. 스킵: title={}, url={}", titleOpt.orElse(null), urlOpt.orElse(null));
                    continue;
                }

                String title = titleOpt.get();
                String content = contentOpt.get();
                String url = urlOpt.get();
                String author = getTagValue(item, "author"); // Or dc:creator, etc.

                String pubDateStr = getTagValue(item, "pubDate");
                LocalDateTime publishedAt = parsePublishedDate(pubDateStr);

                newsList.add(News.create(title, content, url, source, author, language, region, null, publishedAt));
            }
        } catch (NewsException e) {
            throw e;
        } catch (Exception e) {
            throw new NewsException(NewsErrorCode.RSS_PARSE_FAILED, e);
        }
        return newsList;
    }

    private LocalDateTime parsePublishedDate(String pubDateStr) {
        try {
            if (pubDateStr != null && !pubDateStr.isBlank()) {
                return LocalDateTime.parse(pubDateStr, RSS_DATE_FORMATTER);
            }
        } catch (Exception e) {
            log.warn("날짜 파싱 실패. 현재 시간 사용: {}", pubDateStr);
        }
        return LocalDateTime.now();
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        setFeatureIfSupported(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeatureIfSupported(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }

    private void setFeatureIfSupported(DocumentBuilderFactory factory, String feature, boolean value) throws ParserConfigurationException {
        try {
            factory.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
            log.warn("XML parser security feature is not supported: {}", feature);
        }
    }
}
