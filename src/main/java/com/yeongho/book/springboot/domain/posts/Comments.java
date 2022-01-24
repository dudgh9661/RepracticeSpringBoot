package com.yeongho.book.springboot.domain.posts;

import com.yeongho.book.springboot.config.WebSecurityConfig;
import com.yeongho.book.springboot.domain.BaseTimeEntity;
import com.yeongho.book.springboot.exception.InvalidPasswordException;
import com.yeongho.book.springboot.web.dto.CommentsDeleteDto;
import com.yeongho.book.springboot.web.dto.CommentsUpdateRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
public class Comments extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 댓글 테이블 PK

    @Column(nullable = true)
    private Long parentId; // 상위 댓글 ID

    @ManyToOne
    private Posts post;

    @Column(nullable = false)
    private String author;

    @Column(length = 500, nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isDelete;

    @Builder
    public Comments(Long parentId, Posts post, String author, String password, String text) {
        this.parentId = parentId;
        this.post = post;
        this.author = author;
        this.password = password;
        this.text = text;
    }

    public void update(CommentsUpdateRequestDto requestDto) throws InvalidPasswordException {
        if (verifyPassword(requestDto.getPassword())) {
            this.text = requestDto.getText();
        }
    }

    public void delete(CommentsDeleteDto commentsDeleteDto) throws InvalidPasswordException {
        if (verifyPassword(commentsDeleteDto.getPassword())) {
            this.isDelete = true;
        }
    }
    public boolean verifyPassword(String password) throws InvalidPasswordException {
        PasswordEncoder passwordEncoder = new WebSecurityConfig().passwordEncoder();

        if (passwordEncoder.matches(password, this.password)) {
            System.out.println("비밀번호 일치");
            return true;
        } else {
            System.out.println("비밀번호 불일치함");
            System.out.println("해당 게시물 비밀번호 : " + this.password);
            System.out.println("Client로부터 받은 비밀번호 : " + password);
            throw new InvalidPasswordException();
        }
    }
}
