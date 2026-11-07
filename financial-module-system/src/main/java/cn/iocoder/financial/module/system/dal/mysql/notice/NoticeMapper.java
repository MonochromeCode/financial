package cn.iocoder.financial.module.system.dal.mysql.notice;

import cn.iocoder.financial.framework.common.pojo.PageResult;
import cn.iocoder.financial.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.financial.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.financial.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.financial.module.system.dal.dataobject.notice.NoticeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapperX<NoticeDO> {

    default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())
                .orderByDesc(NoticeDO::getId));
    }

}
