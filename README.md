Bu Java uygulaması, log mesajlarına belirli log ID'leri atar, log dizilerinin başlangıç ve bitişini belirtir ve birkaç ayar ile logları TCP üzerinden Elasticsearch'e gönderir.

## Özellikler

- **Log ID'leri:** Her log dizisine benzersiz log ID'leri atar.
- **Başlangıç ve Bitiş İşaretleri:** Log dizilerinin başlangıç ve bitişini açıkça belirtin.
- **Elasticsearch Entegrasyonu:** Logları TCP üzerinden Elasticsearch'e gönderin.

## Başlangıç

### Gereksinimler

- Java 17 veya daha üstü
- Maven

### Kurulum

1. Depoyu klonlayın:

    ```sh
    git clone https://github.com/kullanici-adi/java-loglama-uygulamasi.git
    cd java-loglama-uygulamasi
    ```

2. Maven klasörünüze ekleyin:

    ```sh
    mvn clean install
    ```

### Diğer Projelerinize ekleyin, Konfigürasyon
    
3. diğer projelerinizin `pom.xml` dosyanıza aşağıdaki bağımlılıkları ve eklentileri ekleyin:

    ```xml
		<dependency>
			<groupId>dev.padak</groupId>
			<artifactId>log-quality</artifactId>
			<version>1.0</version>
		</dependency>
    ```

4. `src/main/resources/application.yml` dosyasındaki loglama ayarlarını yapılandırın:

    ```yml
        logquality:
        project_name: testapp
        file: false
        filebeat: false
        logstash:
            connection: true
            host: 192.168.1.15
            port: 31501

        management:
        endpoints:
            web:
            exposure:
                include: metrics
    ```

## Katkıda Bulunma

Katkılar memnuniyetle karşılanır! Lütfen bir pull request gönderin veya değişikliklerinizi tartışmak için bir sorun açın.

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır - detaylar için [LICENSE](LICENSE) dosyasına bakın.
