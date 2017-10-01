package com.app.mvc.acl.service.impl;

import com.app.mvc.acl.condition.PictureCondition;
import com.app.mvc.acl.config.utilConfig;
import com.app.mvc.acl.dao.PictureDao;
import com.app.mvc.acl.entity.Film;
import com.app.mvc.acl.entity.Picture;
import com.app.mvc.acl.service.PictureService;
import com.app.mvc.beans.Page;
import com.app.mvc.beans.StaticTemplateView;
import com.app.mvc.exception.ServiceException;
import com.app.mvc.util.FreemakerUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by wenheng on 2017/7/2.
 */

@Slf4j
@Service
public class PictureServiceImpl implements PictureService {

    @Autowired
    private PictureDao pictureDao;


    public void savePicture(Picture picture) {
        try {
            pictureDao.savePicture(picture);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ServiceException.create("PICTURE.ADD.FALL");
        }
    }

    public void updatePicture(Picture picture) {
        try {
            pictureDao.updatePicture(picture);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ServiceException.create("PICTURE.UPDATE.FAIL");
        }
    }

    public Picture findById(Integer id) {
        Picture picture = null;
        try {
            picture = pictureDao.findById(id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ServiceException.create("PICTURE.FIND.IS.FALL");
        }
        return picture;
    }

    public Page<Picture> queryPicture(PictureCondition pictureCondition) {
        Page<Picture> page = null;
        try {
            List<Picture> list = pictureDao.queryPicture(pictureCondition);
            page = Page.<Picture>builder().data(list).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw ServiceException.create("PICTURE.SEARCH.FAIL");
        }
        return page;
    }

    @Override
    public Map<String, List<Picture>> searchHome() {
        PictureCondition condition = null;
        List<Picture> pictures = null;
        Map<String, List<Picture>> map = Maps.newHashMap();
        try {
            condition = new PictureCondition();
            condition.setPageSize(7);
            condition.setType(utilConfig.PictureType.YZST.name());
            pictures = pictureDao.queryPicture(condition);
            if (pictures != null)
                map.put(utilConfig.PictureType.YZST.name(), pictures);
            condition.setType(utilConfig.PictureType.SWMT.name());
            pictures = pictureDao.queryPicture(condition);
            if (pictures != null)
                map.put(utilConfig.PictureType.SWMT.name(), pictures);
            condition.setType(utilConfig.PictureType.OMXA.name());
            pictures = pictureDao.queryPicture(condition);
            if (pictures != null)
                map.put("OMST", pictures);
            condition.setType(utilConfig.PictureType.TPZP.name());
            pictures = pictureDao.queryPicture(condition);
            if (pictures != null)
                map.put("ZPST", pictures);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return map;
    }

    @Override
    public void staticPictureHtml(String path, Integer id) {
        Picture picture = pictureDao.findById(id);
        Map<String, Object> map = Maps.newHashMap();
        if (picture == null) {
            throw ServiceException.create("PICTURE.THE.ENTITY.IS.NOT.FOUND");
        }
        PictureCondition condition = new PictureCondition();
        condition.setId(id);
        condition.setType(picture.getTypeCode());
        List<Picture> list = pictureDao.queryPictureUpAndDown(condition);
        if (list != null && list.size() > 0) {
            for (Picture pic : list) {
                if (pic.getPictureId() > picture.getPictureId()) {
                    picture.setDownPage(pic);
                } else if (pic.getPictureId() < picture.getPictureId()) {
                    picture.setUpPage(pic);
                }
            }
        }
        picture.setCodeValue(utilConfig.PictureType.valueOf(picture.getTypeCode()).getValue());
        picture.setImgs(picture.getImg().split(";"));
        map.put("picture", picture);
        StaticTemplateView view = new StaticTemplateView();
        view.setFtlPath(path + File.separator + "app" + File.separator + "ftl");
        view.setFltName("picture.ftl");
        view.setDestPath(path + File.separator + "app" + File.separator + "picture" + File.separator + picture.getTypeCode());
        view.setDestName(id + ".html");
        view.setData(map);
        FreemakerUtil.freemakerProcess(view);

    }
}
