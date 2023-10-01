package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.CarPark;
import com.javatechie.spring.batch.listener.StepSkipListener;
import com.javatechie.spring.batch.repository.CarParkRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.io.File;

@Configuration
@EnableBatchProcessing
//@AllArgsConstructor
public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private CarParkRepository carParkRepository;
    @Autowired
    private CarParkItemWriter carParkItemWriter;


    @Bean
    @StepScope
    public FlatFileItemReader<CarPark> itemReader(@Value("#{jobParameters[fullPathFileName]}") String pathToFIle) {
        FlatFileItemReader<CarPark> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new FileSystemResource(new File(pathToFIle)));
        flatFileItemReader.setName("CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;
    }

    private LineMapper<CarPark> lineMapper() {
        DefaultLineMapper<CarPark> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("carParkNo","address","xCoord","yCoord","carParkType","typeOfParkingSystem","shortTermParking","freeParking","nightParking","carParkDecks","gantryHeight","carParkBasement");


        BeanWrapperFieldSetMapper<CarPark> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CarPark.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public CarParkProcessor processor() {
        return new CarParkProcessor();
    }

    @Bean
    public RepositoryItemWriter<CarPark> writer() {
        RepositoryItemWriter<CarPark> writer = new RepositoryItemWriter<>();
        writer.setRepository(carParkRepository);
        writer.setMethodName("save");
        return writer;
    }


    @Bean
    public Step step1(FlatFileItemReader<CarPark> itemReader) {
        return stepBuilderFactory.get("slaveStep").<CarPark, CarPark>chunk(50)
                .reader(itemReader)
                .processor(processor())
                .writer(carParkItemWriter)
                .faultTolerant()
                .listener(skipListener())
                .skipPolicy(skipPolicy())
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    public Job runJob(FlatFileItemReader<CarPark> itemReader) {
        return jobBuilderFactory.get("importCarPark").flow(step1(itemReader)).end().build();
    }


    @Bean
    public SkipPolicy skipPolicy() {
        return new ExceptionSkipPolicy();
    }

    @Bean
    public SkipListener skipListener() {
        return new StepSkipListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(20);
        return taskExecutor;
    }


}
