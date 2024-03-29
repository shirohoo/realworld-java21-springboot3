package sample.shirohoo.realworld.core.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import sample.shirohoo.realworld.core.model.Article;
import sample.shirohoo.realworld.core.model.ArticleDetails;
import sample.shirohoo.realworld.core.model.ArticleFacets;
import sample.shirohoo.realworld.core.model.ArticleFavorite;
import sample.shirohoo.realworld.core.model.ArticleFavoriteRepository;
import sample.shirohoo.realworld.core.model.ArticleRepository;
import sample.shirohoo.realworld.core.model.SocialRepository;
import sample.shirohoo.realworld.core.model.Tag;
import sample.shirohoo.realworld.core.model.User;
import sample.shirohoo.realworld.core.model.UserFollow;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final SocialRepository socialRepository;
    private final ArticleRepository articleRepository;
    private final ArticleFavoriteRepository articleFavoriteRepository;

    /**
     * Get article by slug.
     *
     * @param slug article slug
     * @return Returns article
     */
    public Article readArticleBySlug(String slug) {
        return articleRepository.findBySlug(slug).orElseThrow(() -> new NoSuchElementException("article not found."));
    }

    /**
     * Get articles by facets.
     *
     * @param facets article facets
     * @return Returns articles with information
     */
    public List<ArticleDetails> readArticles(ArticleFacets facets) {
        return articleRepository.findAll(facets).stream()
                .map(articleRepository::findArticleInfoByAnonymous)
                .toList();
    }

    /**
     * Get articles by facets.
     *
     * @param requester user who requested
     * @param facets article facets
     * @return Returns articles with information
     */
    public List<ArticleDetails> readArticles(User requester, ArticleFacets facets) {
        return articleRepository.findAll(facets).stream()
                .map(article -> articleRepository.findArticleInfoByUser(requester, article))
                .toList();
    }

    /**
     * Get articles by my followings.
     *
     * @param user user who requested
     * @param facets article facets
     * @return Returns articles with information
     */
    public List<ArticleDetails> readFeeds(User user, ArticleFacets facets) {
        List<User> following = socialRepository.findByFollower(user).stream()
                .map(UserFollow::getFollowing)
                .toList();

        return articleRepository.findByAuthorInOrderByCreatedAtDesc(following, facets).stream()
                .map(article -> articleRepository.findArticleInfoByUser(user, article))
                .toList();
    }

    /**
     * Write a new article.
     *
     * @param article article
     * @param tags tags
     * @return Returns the written article
     */
    public Article writeArticle(Article article, Collection<Tag> tags) {
        if (articleRepository.existsByTitle(article.getTitle())) {
            throw new IllegalArgumentException("title is already exists.");
        }
        if (tags == null) {
            tags = new HashSet<>();
        }

        return articleRepository.save(article, tags);
    }

    /**
     * Edit article title.
     *
     * @param requester user who requested
     * @param article article
     * @param title new title
     * @return Returns the edited article
     */
    public Article editTitle(User requester, Article article, String title) {
        if (article.isNotAuthor(requester)) {
            throw new IllegalArgumentException("you can't edit articles written by others.");
        }

        if (articleRepository.existsByTitle(title)) {
            throw new IllegalArgumentException("title is already exists.");
        }

        article.setTitle(title);
        return articleRepository.save(article);
    }

    /**
     * Edit article description.
     *
     * @param requester user who requested
     * @param article article
     * @param description new description
     * @return Returns the edited article
     */
    public Article editDescription(User requester, Article article, String description) {
        if (article.isNotAuthor(requester)) {
            throw new IllegalArgumentException("you can't edit articles written by others.");
        }

        article.setDescription(description);
        return articleRepository.save(article);
    }

    /**
     * Edit article content.
     *
     * @param requester user who requested
     * @param article article
     * @param content new content
     * @return Returns the edited article
     */
    public Article editContent(User requester, Article article, String content) {
        if (article.isNotAuthor(requester)) {
            throw new IllegalArgumentException("you can't edit articles written by others.");
        }

        article.setContent(content);
        return articleRepository.save(article);
    }

    /**
     * Delete article.
     *
     * @param requester user who requested
     * @param article article
     */
    public void deleteArticle(User requester, Article article) {
        if (article.isNotAuthor(requester)) {
            throw new IllegalArgumentException("you can't delete articles written by others.");
        }

        articleRepository.delete(article);
    }

    /**
     * Check if the requester has favorited the article.
     *
     * @param requester user who requested
     * @param article article
     * @return Returns true if already favorited
     */
    public boolean isFavorited(User requester, Article article) {
        return articleFavoriteRepository.existsByUserAndArticle(requester, article);
    }

    /**
     * Favorite article.
     *
     * @param requester user who requested
     * @param article article
     */
    public void favoriteArticle(User requester, Article article) {
        if (this.isFavorited(requester, article)) {
            throw new IllegalArgumentException("you already favorited this article.");
        }

        articleFavoriteRepository.save(new ArticleFavorite(requester, article));
    }

    /**
     * Unfavorite article.
     *
     * @param requester user who requested
     * @param article article
     */
    public void unfavoriteArticle(User requester, Article article) {
        if (!this.isFavorited(requester, article)) {
            throw new IllegalArgumentException("you already unfavorited this article.");
        }

        articleFavoriteRepository.deleteByUserAndArticle(requester, article);
    }

    /**
     * Get article information for anonymous.
     *
     * @param article article
     * @return Returns article information
     */
    public ArticleDetails getArticleInfoByAnonymous(Article article) {
        return articleRepository.findArticleInfoByAnonymous(article);
    }

    /**
     * Get article information for user.
     *
     * @param requester user who requested
     * @param article article
     * @return Returns article information
     */
    public ArticleDetails getArticleInfoByUser(User requester, Article article) {
        return articleRepository.findArticleInfoByUser(requester, article);
    }
}
