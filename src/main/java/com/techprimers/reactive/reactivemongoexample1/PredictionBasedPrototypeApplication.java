package com.techprimers.reactive.reactivemongoexample1;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import com.techprimers.reactive.reactivemongoexample1.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.stream.Stream;

@ComponentScan(value = {"com.jtravan.pbs","com.techprimers.reactive.reactivemongoexample1"})
@SpringBootApplication
public class PredictionBasedPrototypeApplication {

    @Bean
    CommandLineRunner employees(EmployeeRepository employeeRepository) {

        return args -> {
            employeeRepository
                    .deleteAll()
                    .subscribe(null, null, () -> {

                        Stream.of(new Employee("1000", "Peter", 23000L),
                                new Employee("1001", "Sam", 13000L),
                                new Employee("1002", "Ryan", 20000L),
                                new Employee("1003", "Chris", 53000L),
                                new Employee("1004", "John", 13000L),
                                new Employee("1005", "Joey", 20000L),
                                new Employee("1006", "Jake", 53000L),
                                new Employee("1007", "Rondey", 13000L),
                                new Employee("1008", "Mark", 20000L),
                                new Employee("1009", "Josh", 53000L),
                                new Employee("1010", "Bob", 13000L),
                                new Employee("1011", "Craig", 20000L),
                                new Employee("1012", "Dave", 53000L),
                                new Employee("1013", "Robert", 13000L),
                                new Employee("1014", "Gene", 20000L),
                                new Employee("1015", "Buster", 53000L),
                                new Employee("1016", "Alex", 13000L),
                                new Employee("1017", "Van", 20000L),
                                new Employee("1018", "Leland", 53000L),
                                new Employee("1019", "Rick", 13000L),
                                new Employee("1020", "Richard", 20000L),
                                new Employee("1021", "Jack", 53000L),
                                new Employee("1022", "Jonathan", 20000L),
                                new Employee("1023", "Trevor", 53000L),
                                new Employee("1024", "R.C.", 20000L),
                                new Employee("1025", "Johnny", 53000L),
                                new Employee("1026", "Wayne", 20000L),
                                new Employee("1027", "Gabriel", 53000L),
                                new Employee("1028", "Danny", 20000L),
                                new Employee("1029", "Daniel", 53000L),
                                new Employee("1030", "Joseph", 20000L),
                                new Employee("1031", "Harry", 53000L),
                                new Employee("1032", "Timothy", 20000L),
                                new Employee("1033", "Paul", 53000L),
                                new Employee("1034", "Jimmy", 20000L),
                                new Employee("1035", "Eric", 53000L),
                                new Employee("1036", "Bruin", 20000L),
                                new Employee("1037", "Chris", 53000L),
                                new Employee("1038", "Frank", 20000L),
                                new Employee("1039", "Matt", 53000L),
                                new Employee("1040", "Matthew", 20000L),
                                new Employee("1041", "Michael", 53000L),
                                new Employee("1042", "Tony", 20000L),
                                new Employee("1043", "Taan", 53000L),
                                new Employee("1044", "Yang", 20000L),
                                new Employee("1045", "Shankar", 53000L),
                                new Employee("1046", "Alonzo", 20000L),
                                new Employee("1047", "D.J.", 53000L),
                                new Employee("1048", "Luke", 53000L),
                                new Employee("1049", "Mike", 20000L),
                                new Employee("1050", "Carl", 53000L),
                                new Employee("1051", "Sammy", 13000L),
                                new Employee("1052", "Ryanard", 20000L),
                                new Employee("1053", "Christopher", 53000L),
                                new Employee("1054", "John John", 13000L),
                                new Employee("1055", "Joey Jo", 20000L),
                                new Employee("1056", "Jacob", 53000L),
                                new Employee("1057", "Rondeizel", 13000L),
                                new Employee("1058", "Markathy", 20000L),
                                new Employee("1059", "Joshua", 53000L),
                                new Employee("1060", "Bobbert", 13000L),
                                new Employee("1061", "Craigarly", 20000L),
                                new Employee("1062", "Daverly", 53000L),
                                new Employee("1063", "Roberto", 13000L),
                                new Employee("1064", "Geneathy", 20000L),
                                new Employee("1065", "Busta Bus", 53000L),
                                new Employee("1066", "Alexander", 13000L),
                                new Employee("1067", "Vananthy", 20000L),
                                new Employee("1068", "Lelandar", 53000L),
                                new Employee("1069", "Rickathy", 13000L),
                                new Employee("1070", "Richy Rich", 20000L),
                                new Employee("1071", "Jacky", 53000L),
                                new Employee("1072", "Michelle", 20000L),
                                new Employee("1073", "Amanda", 53000L),
                                new Employee("1074", "Roshanda", 20000L),
                                new Employee("1075", "Joanna", 53000L),
                                new Employee("1076", "Helen", 20000L),
                                new Employee("1077", "Gabrielle", 53000L),
                                new Employee("1078", "Danielle", 20000L),
                                new Employee("1079", "Dorothy", 53000L),
                                new Employee("1080", "Jessica", 20000L),
                                new Employee("1081", "Harriet", 53000L),
                                new Employee("1082", "Tabatha", 20000L),
                                new Employee("1083", "Pricilla", 53000L),
                                new Employee("1084", "Jennifer", 20000L),
                                new Employee("1085", "Erica", 53000L),
                                new Employee("1086", "Brenda", 20000L),
                                new Employee("1087", "Kristin", 53000L),
                                new Employee("1088", "Fanny", 20000L),
                                new Employee("1089", "Meredith", 53000L),
                                new Employee("1090", "Mandy", 20000L),
                                new Employee("1091", "Mich", 53000L),
                                new Employee("1092", "Tonya", 20000L),
                                new Employee("1093", "Tanya", 53000L),
                                new Employee("1094", "Yang Ying", 20000L),
                                new Employee("1095", "Sheila", 53000L),
                                new Employee("1096", "Alexa", 20000L),
                                new Employee("1097", "Deena", 53000L),
                                new Employee("1098", "Lucy", 53000L),
                                new Employee("1099", "Mary", 20000L),
                                new Employee("1100", "Cathy", 53000L)
                        )
                                .forEach(employee -> {
                                    employeeRepository
                                            .save(employee)
                                            .subscribe(System.out::println);

                                });

                    })
            ;
        };

    }


    public static void main(String[] args) {
        SpringApplication.run(PredictionBasedPrototypeApplication.class, args);
    }
}
