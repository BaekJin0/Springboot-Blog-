package site.metacoding.blogv3.web;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.config.auth.LoginUser;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.service.CategoryService;
import site.metacoding.blogv3.util.Script;
import site.metacoding.blogv3.util.UtilValid;
import site.metacoding.blogv3.web.dto.category.CategoryWriteReqDto;

@RequiredArgsConstructor
@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final HttpSession session;

    @GetMapping("/s/category/writeForm")
    public String writeForm() {
        return "/category/writeForm";
    }

    @PostMapping("/s/category")
    public @ResponseBody String write(
            @Valid CategoryWriteReqDto categoryWriteReqDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginUser loginUser) {

        UtilValid.요청에러처리(bindingResult); // 유효성 검사 끝!!

        // 1. HttpSession에서 get
        // User principal = (User) session.getAttribute("principal"); 머스태치에서 쓰려고 담은 세션

        // 2. Authentication.getPrincipal(); 시큐리티에서 가져온 것
        User principal = loginUser.getUser();

        // 3. getPrincipal() 캐스팅한 후 오브젝트 꺼내주기
        // LoginUser loginU = (LoginUser)
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // User principal = loginU.getUser();

        // 서비스에 기능 호출 (Category 모델에 세션(유저) 담아서!!)
        Category category = categoryWriteReqDto.toEntity(principal);
        categoryService.카테고리등록(category);

        return Script.href("/s/category/writeForm", "카테고리 등록 완료");
    }
}