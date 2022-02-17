package com.yeongho.book.springboot.service.posts;

import com.yeongho.book.springboot.domain.posts.FileItem;
import com.yeongho.book.springboot.domain.posts.FileRepository;
import com.yeongho.book.springboot.domain.posts.Posts;
import com.yeongho.book.springboot.domain.posts.PostsRepository;
import com.yeongho.book.springboot.exception.FileException;
import com.yeongho.book.springboot.exception.InvalidPasswordException;
import com.yeongho.book.springboot.exception.PostsException;
import com.yeongho.book.springboot.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;
    private final FileRepository fileRepository;

    @Transactional
    public Long save(PostsSaveRequestDto postsSaveRequestDto, List<MultipartFile> files) throws FileException, InvalidPasswordException {
        Posts posts = postsSaveRequestDto.toEntity();
        posts.saveFile(files);
        return postsRepository.save(posts).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto, List<MultipartFile> files) throws FileException, InvalidPasswordException {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id =" + id));
        posts.update(requestDto.getAuthor(), requestDto.getPassword(), requestDto.getTitle(), requestDto.getContent(), files);

        return id;
    }

    public PostsResponseDto findById (Long id) {
        Posts post = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        return new PostsResponseDto(post); //entity 객체를 넘겨, PostsResponseDto class에서 이를 가공해 response에 필요한 값들만 뽑아 쓸 것이다.
    }

    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAll () {
        return postsRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime")).stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id, PostsDeleteDto postsDeleteDto) throws FileException, InvalidPasswordException {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id = " + id));
        posts.verifyPassword(postsDeleteDto.getPassword());
//        posts.deleteFile();
        postsRepository.delete(posts);
    }

    public ResponseEntity<Resource> fileDownload(Long id) throws FileException {

        FileItem fileItem = fileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 파일이 없습니다. id = " + id));

        try {
            return fileItem.download();
        } catch (IOException e) {
            throw new FileException();
        }
    }

    public List<PostsListResponseDto> findByCondition(String searchType, String keyword) {
        List<Posts> posts = new ArrayList<>();
        switch(searchType) {
            case "title" :
                posts = postsRepository.findByTitleContaining(keyword);
                break;
            case "content" :
                posts = postsRepository.findByContentContaining(keyword);
                break;
            case "author" :
                posts = postsRepository.findByAuthorContaining(keyword);
                break;
        }

        List<PostsListResponseDto> result = posts.stream().map(PostsListResponseDto::new).collect(Collectors.toList());
        log.info(searchType + "_검색결과 => " + result.toString());
        return result;
    }
}
