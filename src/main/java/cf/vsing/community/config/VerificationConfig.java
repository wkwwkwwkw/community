package cf.vsing.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class VerificationConfig {
    //  注：类名不能为KaptchaConfig,重名会出错
    @SuppressWarnings("SpellCheckingInspection")
    @Bean
    public Producer kaptchaConfig() {
        Properties properties = new Properties();
        properties.put("kaptcha.image.width", "100");
        properties.put("kaptcha.image.height", "40");
        properties.put("kaptcha.textproducer.font.size", "35");
        properties.put("kaptcha.textproducer.font.color", "0,0,0");
        properties.put("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.put("kaptcha.textproducer.char.length", "4");
        properties.put("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;

    }
}
