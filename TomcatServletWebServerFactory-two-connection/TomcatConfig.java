package psychotest.config.profile;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jndi.JndiObjectFactoryBean;
import psychotest.entity.DatasourceEntity;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.List;

@Lazy
@Configuration
public class TomcatConfig {
    private static DatasourceEntity datasourceEntitySource;
    private static DatasourceEntity datasourceEntityTarget;

    public TomcatConfig() {}

    @Bean
    private static void setParameters(List<DatasourceEntity> list) {
        datasourceEntityTarget = list.get(0);
        datasourceEntitySource = list.get(1);
    }

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {

            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                try {
                    ContextResource resource = new ContextResource();
                    resource.setType(DataSource.class.getName());
                    resource.setName("target");
                    setParameters(context, resource, datasourceEntityTarget);

                    ContextResource resource2 = new ContextResource();
                    resource2.setType(DataSource.class.getName());
                    resource2.setName("source");
                    setParameters(context, resource2, datasourceEntitySource);
                } catch (NullPointerException ignored){}
            }
        };
    }

    private void setParameters(Context context, ContextResource resource, DatasourceEntity datasourceEntityTarget) {
        System.out.println("param");
        resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
        resource.setProperty("driverClassName", datasourceEntityTarget.getDriver_name());
        resource.setProperty("url", ""+ datasourceEntityTarget.getUrl()+"?useUnicode=true&serverTimezone=UTC");
        resource.setProperty("username", datasourceEntityTarget.getUsername());
        resource.setProperty("password", datasourceEntityTarget.getPassword());

        context.getNamingResources().addResource(resource);
    }

    @Bean(name = "new_target")
    public DataSource jndiDataSource() throws IllegalArgumentException, NamingException
    {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:/comp/env/target");
        bean.setProxyInterface(DataSource.class);
        bean.setLookupOnStartup(false);
        bean.afterPropertiesSet();

        return (DataSource) bean.getObject();
    }

    @Bean(name = "new_jdbcTemplateTarget")
    public JdbcTemplate jdbcTemplateTarget(@Qualifier("new_target") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "new_source")
    public DataSource jndiDataSource1() throws IllegalArgumentException, NamingException
    {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:/comp/env/source");
        bean.setProxyInterface(DataSource.class);
        bean.setLookupOnStartup(false);
        bean.afterPropertiesSet();

        return (DataSource) bean.getObject();
    }

    @Bean(name = "new_jdbcTemplateSource")
    public JdbcTemplate jdbcTemplateSource(@Qualifier("new_source") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
