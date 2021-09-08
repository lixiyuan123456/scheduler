package com.naixue.nxdp.data.dao.zstream;

import com.naixue.nxdp.data.model.zstream.ZstreamUdx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ZstreamUdxDao
        extends JpaRepository<ZstreamUdx, Integer>, JpaSpecificationExecutor<ZstreamUdx> {

    @Modifying
    @Query(
            value =
                    "update t_zstream_udx t set t.`status` = 0 where t.proxy_code = :proxyCode and t.`name` = :udxName",
            nativeQuery = true)
    void deleteByName(
            @Param("proxyCode") final String proxyCode, @Param("udxName") final String udxName);
}
