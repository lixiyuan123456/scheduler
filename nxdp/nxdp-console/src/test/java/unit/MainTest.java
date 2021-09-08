package unit;

import com.naixue.nxdp.service.hivemetadata.HiveMetadataService;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.naixue.nxdp.Main;
import com.naixue.nxdp.attachment.goshawk.dao.ClusterFlumeTaskRepository;
import com.naixue.nxdp.attachment.goshawk.dao.ClusterHdfsColdWhiteRepository;
import com.naixue.nxdp.attachment.hue.service.HueService;
import com.naixue.nxdp.dao.DailyStatisticRepository;
import com.naixue.nxdp.dao.HadoopBindingRepository;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.dao.JobScheduleRepository;
import com.naixue.nxdp.dao.JobWorkPendingQueueRepository;
import com.naixue.nxdp.dao.MainMenuRepository;
import com.naixue.nxdp.dao.MetadataHiveTableEditionRepository;
import com.naixue.nxdp.dao.MetadataHiveTableRepository;
import com.naixue.nxdp.dao.MetadataLabelRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.dao.mapper.DeprecatedMetadataHiveTableMapper;
import com.naixue.nxdp.dao.mapper.JobExecuteLogMapper;
import com.naixue.nxdp.dao.mapper.MetadataHiveTableMapper;
import com.naixue.zzdp.data.dao.zstream.ZstreamJobDao;
import com.naixue.nxdp.service.AccountService;
import com.naixue.nxdp.service.DailyStatisticService;
import com.naixue.nxdp.service.MainMenuService;
import com.naixue.nxdp.service.MetadataHiveDbService;
import com.naixue.nxdp.service.MetadataHiveTableService;
import com.naixue.nxdp.service.MetadataService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @TestPropertySource(value = "classpath:application.properties")
// @PropertySource(value = "classpath:*.properties", ignoreResourceNotFound = true)
@ActiveProfiles(profiles = {"pre_pro"})
public class MainTest {

  @Autowired MainMenuRepository mainMenuRepository;

  @Autowired MainMenuService mainMenuService;

  @Autowired UserRepository userRepository;

  @Autowired JobScheduleRepository jobScheduleRepository;

  @Autowired JobExecuteLogRepository jobExecuteLogRepository;

  @Autowired DailyStatisticRepository dailyStatisticRepository;

  @Autowired DailyStatisticService dailyStatisticService;

  // @Autowired TimeoutWarningSchedule timeoutWarningSchedule;

  @Autowired HadoopBindingRepository hadoopBindingRepository;

  @Autowired Environment env;

  @Autowired MetadataService metadataService;

  @Autowired HueService hueService;

  @Autowired MetadataHiveDbService metadataHiveDbService;

  @Autowired MetadataLabelRepository metadataLebelRepository;

  @Autowired MetadataHiveTableService metadataHiveTableService;

  @Autowired
  HiveMetadataService hiveMetadataService;

  @Autowired org.apache.tomcat.jdbc.pool.DataSource primaryDataSource;

  @Autowired ClusterFlumeTaskRepository clusterFlumeTaskRepository;

  @Autowired JobExecuteLogMapper jobExecuteLogMapper;

  @Autowired ClusterHdfsColdWhiteRepository clusterHdfsColdWhiteRepository;

  @Autowired ZstreamJobDao jobDao;

  @Autowired private DeprecatedMetadataHiveTableMapper deprecatedMetadataHiveTableMapper;

  @Autowired private MetadataHiveTableRepository metadataHiveTableRepository;

  @Autowired private MetadataHiveTableEditionRepository metadataHiveTableEditionRepository;

  @Autowired private MetadataHiveTableMapper metadataHiveTableMapper;

  @Autowired private JobWorkPendingQueueRepository jobWorkQueueRepository;

  @Autowired private AccountService accountService;

  @Autowired private CuratorFramework zkClient;

  @Transactional
  @Test
  @Rollback(false)
  public void test() throws Exception {
    String namespace = zkClient.getNamespace();
    System.out.println(namespace);
  }
}
