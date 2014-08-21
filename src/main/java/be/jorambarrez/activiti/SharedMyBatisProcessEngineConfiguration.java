package be.jorambarrez.activiti;

import java.lang.reflect.Field;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 */
public class SharedMyBatisProcessEngineConfiguration extends StandaloneProcessEngineConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(SharedMyBatisProcessEngineConfiguration.class);
	
	@Override
	protected Configuration parseMybatisConfiguration(Configuration configuration, XMLConfigBuilder parser) {
		if (ProcessEngines.getProcessEngines().size() == 0) {
			return super.parseMybatisConfiguration(configuration, parser);
		} else {
			try {
				
				ProcessEngineConfigurationImpl firstProcessEngineConfiguration = 
						(ProcessEngineConfigurationImpl) ProcessEngines.getProcessEngines().values().iterator().next().getProcessEngineConfiguration();
				Configuration firstConfiguration = firstProcessEngineConfiguration.getSqlSessionFactory().getConfiguration();
				
				// Set the memory-expensive objects by refercne on the current configuration
    		shareConfigurationField(configuration, firstConfiguration, "sqlFragments");
    		shareConfigurationField(configuration, firstConfiguration, "mappedStatements");
    		shareConfigurationField(configuration, firstConfiguration, "resultMaps");
    		
    		return configuration;
				
			} catch (Exception e) {
				throw new RuntimeException("Could not create Mybatis configuration");
			}
		}
	}
	
	/**
   * Share a specific field of the MyBatis Configuration. 
   * @param configuration the current configuration
   * @param sharedObjectFieldName the field to be shared
   */
  private static void shareConfigurationField(final Configuration configuration, 
  		final Configuration firstConfiguration, final String sharedObjectFieldName) {
  	
      Throwable exception = null;
      try {
          // get the field to share 
          Field sharedField = Configuration.class.getDeclaredField(sharedObjectFieldName);
          sharedField.setAccessible(true);
          Object sharedObject = sharedField.get(firstConfiguration);
 
          // we got a shared object => use it in the configuration
          sharedField.set(configuration, sharedObject);
 
      } catch (NoSuchFieldException e) {
          exception = e;
      } catch (IllegalArgumentException e) {
          exception = e;
      } catch (IllegalAccessException e) {
          exception = e;
      }
      if (exception != null) {
          exception.printStackTrace();
      }
  }
	
}
