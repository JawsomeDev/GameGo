package com.gamego.controller;


import com.gamego.domain.account.Account;
import com.gamego.domain.account.CurrentAccount;
import com.gamego.domain.review.Review;
import com.gamego.domain.review.form.ReviewForm;
import com.gamego.domain.room.Room;
import com.gamego.repository.ReviewRepository;
import com.gamego.service.ReviewService;
import com.gamego.service.query.ReviewQueryService;
import com.gamego.service.query.RoomQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final RoomQueryService roomQueryService;
    private final ReviewQueryService reviewQueryService;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;


    @GetMapping("/room/{path}/reviews")
    public String reviewRoom(@CurrentAccount Account account, @PathVariable String path,
                             @RequestParam(required = false) Long editReviewId,
                             @PageableDefault(size=12, sort = "createdAt") Pageable pageable, Model model) {
        Room room = roomQueryService.getRoom(path);
        Page<Review> reviews = reviewQueryService.getReviews(room.getId(), pageable);
        Double averageRating = reviewQueryService.calculateAverageRating(reviews);
        model.addAttribute(room);
        model.addAttribute(account);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute(new ReviewForm());
        if(editReviewId != null) {
            Review review = reviewRepository.findById(editReviewId).orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
            ReviewForm reviewForm = modelMapper.map(review, ReviewForm.class);
            model.addAttribute("reviewForm", reviewForm);
        }else {
            model.addAttribute("reviewForm", new ReviewForm());
        }

        model.addAttribute("editReviewId", editReviewId);
        checkAuth(account, model, room);
        return "room/reviews";
    }

    @PostMapping("/room/{path}/reviews")
    public String postReview(@CurrentAccount Account account, @PathVariable String path,
                             @Valid ReviewForm reviewForm, BindingResult bindingResult, Model model, Pageable pageable,
                             RedirectAttributes attributes, ModelMap modelMap) {
        Room room = roomQueryService.getRoom(path);

        if(bindingResult.hasErrors()){
            model.addAttribute(room);
            Page<Review> reviews = reviewQueryService.getReviews(room.getId(), pageable);
            model.addAttribute("reviews", reviews);
            checkAuth(account, model, room);
            model.addAttribute(account);

            return "room/reviews";
        }
        reviewService.addReview(path, account, reviewForm);
        attributes.addFlashAttribute("message", "리뷰가 등록되었습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/reviews";
    }

    @PostMapping("/room/{path}/reviews/edit")
    public String updateReview(@CurrentAccount Account account, @PathVariable String path,
                               @Valid ReviewForm reviewForm, BindingResult bindingResult, Model model,
                               Pageable pageable, RedirectAttributes attributes, @RequestParam Long reviewId) {
        Room room = roomQueryService.getRoom(path);
        if(bindingResult.hasErrors()){
            model.addAttribute(room);
            Page<Review> reviews = reviewQueryService.getReviews(room.getId(), pageable);
            model.addAttribute("reviews", reviews);
            checkAuth(account, model, room);
            model.addAttribute(account);
            model.addAttribute("editReviewId", reviewId);
            return "room/reviews";
        }
        reviewService.updateReview(room, account, reviewForm);
        attributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/reviews";
    }

    @PostMapping("/room/{path}/reviews/delete")
    public String deleteReview(@CurrentAccount Account account, @PathVariable String path,
                               @RequestParam Long reviewId,
                               RedirectAttributes attributes) {
        Room room = roomQueryService.getRoom(path);
        reviewService.deleteReview(room, account);
        attributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
        return "redirect:/room/" + room.getEncodedPath() + "/reviews";
    }


    private void checkAuth(Account account, Model model, Room room) {
        boolean hasReview = reviewQueryService.findByRoomAndAccount(room, account);
        model.addAttribute("hasReview", hasReview);
        boolean isMaster = roomQueryService.isMaster(account, room);
        model.addAttribute("isMaster", isMaster);
        boolean isManagerOrMaster = roomQueryService.isManagerOrMaster(account, room);
        model.addAttribute("isManagerOrMaster", isManagerOrMaster);
        boolean isMemberOrManager = roomQueryService.isMemberOrManager(account, room);
        model.addAttribute("isMemberOrManager", isMemberOrManager);
    }
}
