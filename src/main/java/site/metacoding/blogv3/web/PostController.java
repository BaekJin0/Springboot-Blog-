package site.metacoding.blogv3.web;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.config.auth.LoginUser;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.service.PostService;
import site.metacoding.blogv3.web.dto.post.PostResponseDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;

    @GetMapping("/user/{id}/post")
    public String postList(Integer categoryId, @PathVariable Integer id, @AuthenticationPrincipal LoginUser loginUser,
            Model model, @PageableDefault(size = 3) Pageable pageable) {

        PostResponseDto postResponseDto = null;

        if (categoryId == null) {
            // SELECT * FROM category WHRER userId = :id
            postResponseDto = postService.게시글목록보기(id, pageable);
        } else {
            // SELECT * FROM post WHERE userId = :id AND categoryId = :categoryId
            postResponseDto = postService.카테고리별게시글목록보기(id, categoryId, pageable);
        }

        model.addAttribute("postResponseDto", postResponseDto);

        return "/post/list";
    }

    @GetMapping("/s/post/write-form")
    public String writeForm(@AuthenticationPrincipal LoginUser loginUser, Model model) {
        List<Category> categories = postService.게시글쓰기화면(loginUser.getUser());

        if (categories.size() == 0) {
            throw new CustomException("카테고리 등록이 필요합니다.");
        }

        model.addAttribute("categories", categories);
        return "/post/writeForm";
    }

    @PostMapping("/s/post")
    public String write(PostWriteReqDto postWriteReqDto,
            @AuthenticationPrincipal LoginUser loginUser) {

        postService.게시글쓰기(postWriteReqDto, loginUser.getUser());
        return "redirect:/user/" + loginUser.getUser().getId() + "/post";
    }
}
