package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {


    //@Value("#{environment.SOME_KEY_PROPERTY}")
    //private String key;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }


    @Bean
    public DatabaseServiceCredentials getDBServiceCredentials(@Value("${VCAP_SERVICES}") String key){
        return new DatabaseServiceCredentials(key);
    }

    @Bean
    public HibernateJpaVendorAdapter getAdapter(){

        HibernateJpaVendorAdapter myAdapter =  new HibernateJpaVendorAdapter();
        myAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        myAdapter.setGenerateDdl(true);
        myAdapter.setDatabase(Database.MYSQL);
        return myAdapter;
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
      //  MysqlDataSource dataSource = new MysqlDataSource();
      //  dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
      //  return dataSource;


        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariConfig cfg = new HikariConfig();

        cfg.setDataSource(dataSource);
        return new HikariDataSource(cfg);

    }


    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {


        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));

        HikariConfig cfg = new HikariConfig();

        cfg.setDataSource(dataSource);
        return new HikariDataSource(cfg);

       /* MysqlDataSource dataSource = new MysqlDataSource();
        HikariDataSource hikariDataSource = new HikariDataSource();
        boolean wrapperFor = hikariDataSource.isWrapperFor( <MysqlDataSource>dataSource);
        */

       // dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));


    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesContainerEntityManagerBean(DataSource moviesDataSource, HibernateJpaVendorAdapter adapter){
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource);
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        factoryBean.setPersistenceUnitName("moviesdb");
        return factoryBean;
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean albumsContainerEntityManagerBean(DataSource albumsDataSource, HibernateJpaVendorAdapter adapter){
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource);
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        factoryBean.setPersistenceUnitName("albumsdb");
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManagerBean(EntityManagerFactory moviesContainerEntityManagerBean){
        return new JpaTransactionManager(moviesContainerEntityManagerBean);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManagerBean(EntityManagerFactory albumsContainerEntityManagerBean){
        return new JpaTransactionManager(albumsContainerEntityManagerBean);
    }


    @Bean
    public TransactionTemplate albumstransactionTemplate(PlatformTransactionManager albumsPlatformTransactionManagerBean){
       return new      TransactionTemplate(albumsPlatformTransactionManagerBean);
    }


    @Bean
    public TransactionTemplate moviestransactionTemplate(PlatformTransactionManager moviesPlatformTransactionManagerBean){
        return new      TransactionTemplate(moviesPlatformTransactionManagerBean);
    }

    //  this.albumstransactionTemplate = new TransactionTemplate(albumsPlatformTransactionManagerBean);
    //  this.moviestransactionTemplate = new TransactionTemplate(moviesPlatformTransactionManagerBean);

}
