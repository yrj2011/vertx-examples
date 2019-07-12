package my.cluster;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 读取配置文件工具类
 */
public class ResourceLoader {
  private static ResourceLoader loader = new ResourceLoader();
  private static Map<String, Properties> loadermap = new HashMap<String,Properties>();
  public static String DEFAULT_CONFIG_FILE;
  private ResourceLoader(){}

  public static ResourceLoader getInstance(){
    return loader;
  }

  public Properties getPropFromProperties(String fileName) throws Exception{
    Properties prop = loadermap.get(fileName);
    if(prop!=null){
      return prop;
    }
    prop = new Properties();
    prop.load(new FileInputStream(new File(fileName)));
    loadermap.put(fileName, prop);
    return prop;
  }


  public  String getString(String key) throws Exception{
    Properties prop = ResourceLoader.getInstance().getPropFromProperties(DEFAULT_CONFIG_FILE);
    String value = prop.getProperty(key);
    return value;
  }

  public  Integer getInt(String key) throws Exception{
    return Integer.parseInt(getString(key));
  }

  public  Long getLong(String key) throws NumberFormatException, Exception{
    return Long.parseLong(getString(key));
  }

  public  Boolean getBoolean(String key) throws Exception{
    return Boolean.parseBoolean(getString(key));
  }

  public static void main(String[] args) throws Exception{
    System.out.println(loader.getString("vertx.VXWEB.cluster.enabled"));
  }
}
