package com.yeongho.book.springboot.web.dto;

import com.yeongho.book.springboot.domain.posts.Posts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor
public class PostsListResponseDto {
    private Long id;
    private String title;
    private String author;
    private int liked;
    private LocalDateTime createdDate;

    public PostsListResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.liked = entity.getLiked();
        this.createdDate = entity.getCreatedDate();
    }
}
