package site.metacoding.blogv3.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostResponseDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Service
public class PostService {
    @Value("${file.path}") // yml에 등록한 키 값 찾을 때 사용하는 어노테이션
    private String uploadFolder;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public List<Category> 게시글쓰기화면(User principal) {
        return categoryRepository.findByUserId(principal.getId());
    }

    public PostResponseDto 게시글목록보기(Integer userId, Pageable pageable) {
        // 카테고리목록보기
        List<Category> categoriesEntity = categoryRepository.findByUserId(userId);
        Page<Post> postsEntity = postRepository.findByUserId(userId, pageable);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }

        return new PostResponseDto(
                postsEntity,
                categoriesEntity,
                userId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers);
    }

    public PostResponseDto 카테고리별게시글목록보기(Integer userId, Integer categoryId, Pageable pageable) {
        List<Category> categoriesEntity = categoryRepository.findByUserId(userId);
        Page<Post> postsEntity = postRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }

        return new PostResponseDto(
                postsEntity,
                categoriesEntity,
                userId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers);
    }

    // 하나의 서비스는 여러가지 일을 한번에 처리한다. (여러가지 일이 하나의 트랜잭션이다.)
    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto, User principal) {
        // 서비스는 여러가지 로직이 공존한다. -> 단점 : 디버깅하기 힘들다.
        // 1. 이미지 파일 저장 (UUID 변경) 후 경로 리턴 받기
        String thumnail = null;
        if (!postWriteReqDto.getThumnailFile().isEmpty()) {
            thumnail = UtilFileUpload.write(uploadFolder, postWriteReqDto.getThumnailFile());
        }
        // id만 넣어서 INSERT 하는 건 버전 업데이트 하면서 막혔음
        // Category category = new Category();
        // category.setId(postWriteReqDto.getCategoryId());
        // 2. 카테고리 있는지 확인
        Optional<Category> categoryOp = categoryRepository.findById(postWriteReqDto.getCategoryId());
        if (categoryOp.isPresent()) {
            // 3. post DB 저장
            // (1) 이미지 파일명을 Post 오브젝트 thumnail에 옮기기
            // (2) title, content도 Post 오브젝트에 옮기기
            // (3) userId(user 오브젝트)도 Post 오브젝트에 옮기기
            // (4) categoryId(category 오브젝트)도 Post 오브젝트에 옮기기
            // (5) save
            Post post = postWriteReqDto.toEntity(thumnail, principal, categoryOp.get());
            postRepository.save(post);
        } else {
            throw new CustomException("해당 카테고리가 존재하지 않습니다.");
        }
        // 오브젝트(ORM)가 아닌 FK를 직접 주입하는 방법의 단점 : 없는 FK가 들어올 수 있다 -> 막을 수 없음
        // postRepository.mSave(postWriteReqDto.getCategoryId(), principal.getId(),
        // postWriteReqDto.getTitle(),
        // postWriteReqDto.getContent(), thumnail);
    }
}