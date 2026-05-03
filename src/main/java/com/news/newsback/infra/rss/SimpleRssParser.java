package com.news.newsback.infra.rss;

import com.news.newsback.domain.news.model.News;
import com.news.newsback.domain.news.exception.NewsErrorCode;
import com.news.newsback.domain.news.exception.NewsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
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
import java.util.regex.Pattern;

@Slf4j
@Component
public class SimpleRssParser implements RssParser {

    private static final DateTimeFormatter RSS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private static final Pattern BARE_HTML_ENTITY_PATTERN = Pattern.compile(
            "(?<!&)(hellip|ldquo|rdquo|lsquo|rsquo|quot|apos|middot|nbsp|amp|lt|gt);"
    );
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

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
                
                Optional<String> titleOpt = Optional.ofNullable(getTagValue(item, "title"))
                        .map(this::normalizeHtmlText)
                        .filter(value -> !value.isBlank());
                Optional<String> contentOpt = getFirstTagValue(item, "description", "content:encoded", "dc:subject", "title")
                        .map(this::normalizeHtmlText)
                        .filter(value -> !value.isBlank());
                Optional<String> urlOpt = Optional.ofNullable(getTagValue(item, "link"))
                        .map(this::normalizeHtmlText)
                        .filter(value -> !value.isBlank());

                if (titleOpt.isEmpty() || contentOpt.isEmpty() || urlOpt.isEmpty()) {
                    log.warn("RSS item 필수 필드 누락. 스킵: title={}, url={}", titleOpt.orElse(null), urlOpt.orElse(null));
                    continue;
                }

                String title = titleOpt.get();
                String content = contentOpt.get();
                String url = urlOpt.get();
                String author = Optional.ofNullable(getTagValue(item, "author"))
                        .map(this::normalizeHtmlText)
                        .orElse(null); // Or dc:creator, etc.

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

    private Optional<String> getFirstTagValue(Element element, String... tagNames) {
        for (String tagName : tagNames) {
            String value = getTagValue(element, tagName);
            if (value != null && !normalizeHtmlText(value).isBlank()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private String normalizeHtmlText(String value) {
        String repaired = BARE_HTML_ENTITY_PATTERN.matcher(value).replaceAll("&$1;");
        String unescaped = HtmlUtils.htmlUnescape(repaired).replace('\u00A0', ' ');
        String withoutTags = HTML_TAG_PATTERN.matcher(unescaped).replaceAll(" ");
        return withoutTags.replaceAll("\\s+", " ").strip();
    }

    private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
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
