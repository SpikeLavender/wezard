package com.natsumes.wezard.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.CommentForm;
import com.natsumes.wezard.enums.ResponseEnum;
import com.natsumes.wezard.pojo.Comment;
import com.natsumes.wezard.service.CommentService;
import com.natsumes.wezard.service.DubboCommentService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Reference
    private DubboCommentService dubboCommentService;

    @Override
    public Response addComment(CommentForm commentForm) {

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentForm, comment);

        int i = dubboCommentService.insert(comment);
        if (i <= 0) {
            return Response.error(ResponseEnum.SYSTEM_ERROR, "添加评论失败");
        }
        return Response.success();
    }

    @Override
    public Response deleteComment(Integer id) {

        dubboCommentService.deleteByPrimaryKey(id);
        return Response.success();
    }

    @Override
    public Response<PageInfo> selectComment(Integer userId, Integer productId, Integer pageNum, Integer pageSize) {
        Comment comment = new Comment();
        comment.setProductId(productId);
        comment.setUserId(userId);
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> comments = dubboCommentService.selectSelective(comment);
        PageInfo pageInfo = new PageInfo<>(comments);
        //todo: 做一些 id 联查的处理等
        return Response.success(pageInfo);
    }

    @Override
    public Response updateComment(Comment comment) {
        int i = dubboCommentService.updateByPrimaryKeySelective(comment);
        if (i <= 0) {
            return Response.error(ResponseEnum.SYSTEM_ERROR, "更新评论失败");
        }
        return Response.success("更新成功");
    }
}
