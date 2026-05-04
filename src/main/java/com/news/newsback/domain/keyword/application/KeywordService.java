package com.news.newsback.domain.keyword.application;

import com.news.newsback.domain.keyword.domain.KeywordRepository;
import com.news.newsback.domain.keyword.domain.UserKeywordRepository;
import com.news.newsback.domain.keyword.domain.Keyword;
import com.news.newsback.domain.keyword.domain.UserKeyword;
import com.news.newsback.domain.keyword.exception.KeywordErrorCode;
import com.news.newsback.domain.keyword.util.KeywordNormalizer;
import com.news.newsback.domain.user.domain.User;
import com.news.newsback.domain.user.domain.UserRepository;
import com.news.newsback.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.news.newsback.domain.keyword.dto.KeywordDto;

// 키워드 서비스: 유저 키워드 등록, 삭제, 구독 해제 로직 포함
@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    // 키워드 등록 로직: 중복, 개수 제한 검증 포함
    public void registerKeyword(Long userId, String rawKeyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(KeywordErrorCode.USER_NOT_FOUND));

        if (userKeywordRepository.countByUserId(userId) >= 5) {
            throw new BusinessException(KeywordErrorCode.MAX_KEYWORDS_EXCEEDED);
        }

        String normalizedKeyword = KeywordNormalizer.normalize(rawKeyword);
        Keyword keyword = keywordRepository.findByName(normalizedKeyword)
                .orElseGet(() -> keywordRepository.save(new Keyword(normalizedKeyword)));

        if (userKeywordRepository.existsByUserIdAndKeywordId(userId, keyword.getId())) {
            throw new BusinessException(KeywordErrorCode.DUPLICATE_KEYWORD);
        }

        userKeywordRepository.save(new UserKeyword(user.getId(), keyword));
    }

    public void unsubscribeKeyword(Long userId, Long keywordId) {
        // Debugging log for userId and keywordId
        System.out.println("User ID: " + userId);
        System.out.println("Keyword ID: " + keywordId);

        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new BusinessException(KeywordErrorCode.KEYWORD_NOT_FOUND));

        System.out.println("Found Keyword: " + keyword);

        if (!userKeywordRepository.existsByUserIdAndKeywordId(userId, keyword.getId())) {
            throw new BusinessException(KeywordErrorCode.KEYWORD_NOT_SUBSCRIBED);
        }

        userKeywordRepository.deleteByUserIdAndKeywordId(userId, keyword.getId());
    }

    public List<String> retrievePopularKeywords() {
        return userKeywordRepository.findPopularKeywords();
    }

    public List<KeywordDto> retrieveKeywordsByUserId(Long userId) {
        return userKeywordRepository.findByUserId(userId)
                .stream()
                .map(userKeyword -> new KeywordDto(userKeyword.getKeyword().getId(), userKeyword.getKeyword().getName()))
                .collect(Collectors.toList());
    }
}
