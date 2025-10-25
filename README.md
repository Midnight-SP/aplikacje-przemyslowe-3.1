# Prosty poradnik: Twoja pierwsza aplikacja Spring Boot z Dependency Injection

## Wprowadzenie — co będziemy budować?

W tym poradniku stworzymy prostą aplikację do zarządzania biblioteką książek. Aplikacja będzie bardzo minimalistyczna, ale pokaże najważniejsze koncepcje Spring Boot, które są wymagane w Twoim zadaniu. Skupimy się na trzech kluczowych elementach: definiowaniu serwisów jako beanów Spring, wstrzykiwaniu zależności między nimi oraz konfigurowaniu zewnętrznych obiektów. Nie będziemy implementować wszystkich skomplikowanych operacji z zadania, ale po przejściu przez ten przykład zrozumiesz fundamenty, które pozwolą Ci wykonać pełne zadanie.

Nasza biblioteka będzie miała tylko trzy proste funkcjonalności: przechowywanie książek w pamięci, wyszukiwanie ich po autorze oraz wyświetlanie podstawowych statystyk. To wystarczy, żeby pokazać jak Spring zarządza obiektami i jak komponenty współpracują ze sobą bez ręcznego tworzenia instancji.

---

## Krok 1: Konfiguracja projektu Maven

Zacznijmy od najważniejszego pliku konfiguracyjnego, który mówi Maven jakie biblioteki potrzebujemy. Plik `pom.xml` definiuje wszystkie zależności naszego projektu.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>library-app</artifactId>
    <version>1.0.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Ten plik konfiguracyjny definiuje kilka kluczowych elementów naszego projektu. Sekcja `parent` wskazuje na specjalny projekt Spring Boot, który dostarcza nam gotowe konfiguracje dla wszystkich popularnych bibliotek. Dzięki temu nie musimy ręcznie określać wersji każdej zależności, ponieważ Spring Boot wie które wersje bibliotek współpracują ze sobą najlepiej. Następnie w sekcji `dependencies` dodajemy `spring-boot-starter`, który jest podstawowym pakietem zawierającym wszystko co potrzebne do uruchomienia aplikacji Spring Boot. Na końcu plugin Maven pozwala nam uruchomić aplikację komendą `mvn spring-boot:run` oraz zbudować wykonywalny plik JAR zawierający całą aplikację wraz z wbudowanym serwerem.

---

## Krok 2: Model danych — klasa Book

Teraz stworzymy prostą klasę reprezentującą książkę w naszej bibliotece. To będzie zwykła klasa Java bez żadnych adnotacji Spring, ponieważ obiekty książek nie są beanami zarządzanymi przez kontener Spring.

```java
package com.example.library.model;

public class Book {
    private String title;
    private String author;
    private int year;

    // Constructor needed for creating books
    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", year=" + year +
                '}';
    }
}
```

Klasa `Book` jest bardzo prostym obiektem, który nazywamy często modelem danych lub encją domenową. Zawiera ona tylko trzy pola opisujące książkę oraz publiczny konstruktor, który będzie potrzebny później do tworzenia instancji. Ważne jest zrozumienie, że obiekty typu `Book` nie będą zarządzane przez Spring Container. Możemy tworzyć je normalnie używając operatora `new`, ponieważ są to po prostu obiekty przechowujące dane. Spring zarządza tylko klasami logiki biznesowej, które nazywamy serwisami, ale nie zarządza każdym obiektem w aplikacji. Metoda `toString` jest przydatna do wyświetlania informacji o książce w czytelnej formie.

---

## Krok 3: Serwis do zarządzania książkami

Teraz stworzymy nasz pierwszy serwis, który będzie zarządzany przez Spring. To tutaj dzieje się magia Dependency Injection.

```java
package com.example.library.service;

import com.example.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final List<Book> books;

    // Spring will automatically call this constructor
    public BookService() {
        this.books = new ArrayList<>();
        System.out.println("BookService has been created by Spring!");
    }

    public void addBook(Book book) {
        books.add(book);
        System.out.println("Added book: " + book.getTitle());
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> findBooksByAuthor(String author) {
        return books.stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public int getTotalBooks() {
        return books.size();
    }
}
```

Ta klasa jest sercem naszej aplikacji i pokazuje kluczową koncepcję Spring Boot. Adnotacja `@Service` nad klasą mówi Spring, że ta klasa powinna być zarządzana jako bean. Co to oznacza w praktyce? Spring automatycznie utworzy dokładnie jedną instancję tej klasy podczas startu aplikacji i będzie ją przechowywał w swoim kontenerze. Kiedy jakaś inna klasa będzie potrzebowała `BookService`, Spring automatycznie dostarczy jej tę samą instancję. Nie musimy nigdzie pisać `new BookService()`, ponieważ Spring robi to za nas. Metody w serwisie oferują podstawowe operacje na książkach, a używanie Stream API do filtrowania po autorze pokazuje nowoczesne podejście do przetwarzania kolekcji w Javie.

---

## Krok 4: Serwis statystyk z prostszymi metodami

```java
package com.example.library.service;

import com.example.library.model.Book;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    private final BookService bookService;

    // Spring sees that this constructor needs BookService
    // and automatically provides it!
    public StatisticsService(BookService bookService) {
        this.bookService = bookService;
        System.out.println("StatisticsService has been created with BookService injected!");
    }

    public void printLibraryStatistics() {
        List<Book> allBooks = bookService.getAllBooks();
        System.out.println("\n=== Library Statistics ===");
        System.out.println("Total books: " + allBooks.size());

        if (!allBooks.isEmpty()) {
            // Count books per author using a simple Map and loop
            Map<String, Integer> booksByAuthor = new HashMap<>();

            for (Book book : allBooks) {
                String author = book.getAuthor();

                // If author already in map, increase count by 1
                if (booksByAuthor.containsKey(author)) {
                    int currentCount = booksByAuthor.get(author);
                    booksByAuthor.put(author, currentCount + 1);
                } else {
                    // If author not in map yet, start with count 1
                    booksByAuthor.put(author, 1);
                }
            }

            System.out.println("\nBooks per author:");
            for (String author : booksByAuthor.keySet()) {
                int count = booksByAuthor.get(author);
                System.out.println("  " + author + ": " + count + " book(s)");
            }
        }
    }
}
```

Ta wersja metody robi dokładnie to samo co poprzednia, ale używa znacznie prostszych konstrukcji (pętle, `if-else`, mapa `HashMap`). To może być bardziej czytelne dla osób na początku nauki.

---

## Krok 5: Klasa konfiguracyjna z zewnętrznymi beanami

Czasami chcemy udostępnić jako beany obiekty z zewnętrznych bibliotek, które nie mają adnotacji Spring. Wtedy używamy klasy konfiguracyjnej.

```java
package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    // This method creates a bean that can be injected elsewhere
    @Bean
    public DateTimeFormatter dateFormatter() {
        System.out.println("Creating DateTimeFormatter bean...");
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    // Another example: creating a custom object as a bean
    @Bean
    public String libraryName() {
        return "Central City Library";
    }
}
```

Klasa konfiguracyjna oznaczona adnotacją `@Configuration` to specjalne miejsce, gdzie możemy definiować beany poprzez metody zamiast adnotacji na klasach. Każda metoda z adnotacją `@Bean` zostanie wywołana przez Spring dokładnie raz podczas startu aplikacji, a zwrócony obiekt będzie dostępny do wstrzykiwania.

---

## Krok 6: Konfiguracja przez application.properties

Spring Boot pozwala na przechowywanie konfiguracji w zewnętrznym pliku, dzięki czemu możemy zmieniać parametry bez modyfikacji kodu.

Plik: `src/main/resources/application.properties`

```properties
# Application settings
app.library.name=Central City Library
app.library.max-books=1000

# Logging configuration
logging.level.root=INFO
logging.level.com.example.library=DEBUG
```

Właściwości z tego pliku możemy wstrzyknąć do naszych klas używając adnotacji `@Value` (lub podejścia `@ConfigurationProperties`).

Przykład użycia:

```java
package com.example.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LibraryInfoService {
    private final String libraryName;
    private final int maxBooks;

    // Spring will inject values from application.properties
    public LibraryInfoService(
            @Value("${app.library.name}") String libraryName,
            @Value("${app.library.max-books}") int maxBooks) {
        this.libraryName = libraryName;
        this.maxBooks = maxBooks;
        System.out.println("Library initialized: " + libraryName);
    }

    public void printInfo() {
        System.out.println("\nLibrary: " + libraryName);
        System.out.println("Maximum capacity: " + maxBooks + " books");
    }

    public boolean canAddMoreBooks(int currentCount) {
        return currentCount < maxBooks;
    }
}
```

---

## Krok 7: Definiowanie beanów w pliku XML

Spring pozwala również na definiowanie beanów w plikach XML (starsze, ale wspierane podejście). Pokażemy to na prostym przykładzie.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- src/main/resources/books-config.xml -->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/util
           http://www.springframework.org/schema/util/spring-util.xsd">

    <!-- Define individual book beans -->
    <bean id="book1" class="com.example.library.model.Book">
        <constructor-arg value="1984"/>
        <constructor-arg value="George Orwell"/>
        <constructor-arg value="1949"/>
    </bean>

    <bean id="book2" class="com.example.library.model.Book">
        <constructor-arg value="To Kill a Mockingbird"/>
        <constructor-arg value="Harper Lee"/>
        <constructor-arg value="1960"/>
    </bean>

    <bean id="book3" class="com.example.library.model.Book">
        <constructor-arg value="Pride and Prejudice"/>
        <constructor-arg value="Jane Austen"/>
        <constructor-arg value="1813"/>
    </bean>

    <!-- Collect all books into a list bean -->
    <util:list id="predefinedBooks" value-type="com.example.library.model.Book">
        <ref bean="book1"/>
        <ref bean="book2"/>
        <ref bean="book3"/>
    </util:list>
</beans>
```

Plik XML pokazuje alternatywny sposób definiowania beanów bez używania adnotacji w kodzie Java. Każdy element `<bean>` tworzy jeden obiekt; elementy `<constructor-arg>` przekazują wartości do konstruktora w odpowiedniej kolejności. Element `<util:list>` tworzy bean będący listą referencji do wcześniej zdefiniowanych beanów.

---

## Krok 8: Główna klasa aplikacji

Teraz połączymy wszystko razem w klasie startowej, która będzie punktem wejścia do naszej aplikacji.

```java
package com.example.library;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import com.example.library.service.LibraryInfoService;
import com.example.library.service.StatisticsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.List;

@SpringBootApplication
@ImportResource("classpath:books-config.xml")
public class LibraryApplication implements CommandLineRunner {

    // All dependencies injected through constructor
    private final BookService bookService;
    private final StatisticsService statisticsService;
    private final LibraryInfoService libraryInfoService;
    private final List<Book> predefinedBooks;

    public LibraryApplication(
            BookService bookService,
            StatisticsService statisticsService,
            LibraryInfoService libraryInfoService,
            List<Book> predefinedBooks) {
        this.bookService = bookService;
        this.statisticsService = statisticsService;
        this.libraryInfoService = libraryInfoService;
        this.predefinedBooks = predefinedBooks;
    }

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("Starting Library Application");
        System.out.println("========================================\n");

        // Display library info
        libraryInfoService.printInfo();

        // Add books from XML configuration
        System.out.println("\nLoading predefined books from XML...");
        for (Book book : predefinedBooks) {
            bookService.addBook(book);
        }

        // Add some additional books manually
        System.out.println("\nAdding more books...");
        bookService.addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925));
        bookService.addBook(new Book("Animal Farm", "George Orwell", 1945));

        // Display all books
        System.out.println("\n=== All Books in Library ===");
        for (Book book : bookService.getAllBooks()) {
            System.out.println(book);
        }

        // Search for books by author
        System.out.println("\n=== Books by George Orwell ===");
        List<Book> orwellBooks = bookService.findBooksByAuthor("George Orwell");
        orwellBooks.forEach(System.out::println);

        // Show statistics
        statisticsService.printLibraryStatistics();

        System.out.println("\n========================================");
        System.out.println("Application finished successfully!");
        System.out.println("========================================\n");
    }
}
```

To jest serce naszej aplikacji, które łączy wszystkie elementy w działającą całość. Adnotacja `@SpringBootApplication` jest kombinacją trzech innych adnotacji i mówi Spring, żeby zeskanował wszystkie klasy w tym pakiecie i podpakietach w poszukiwaniu komponentów, włączył autokonfigurację oraz oznaczył tę klasę jako źródło definicji beanów. Adnotacja `@ImportResource` ładuje nasz plik XML z definicjami książek, dzięki czemu beany z tego pliku stają się dostępne w kontekście Spring. Implementacja interfejsu `CommandLineRunner` pozwala wykonać kod automatycznie po pełnym zainicjowaniu aplikacji.

---

## Uruchomienie aplikacji

Aby uruchomić aplikację, wykonaj w terminalu:

```powershell
mvn spring-boot:run
```

Po uruchomieniu zobaczysz w konsoli komunikaty pokazujące jak Spring tworzy beany, wstrzykuje zależności i wykonuje naszą logikę biznesową.

---

## Podsumowanie — kluczowe koncepcje

- `@Service` przekształca zwykłą klasę w komponent zarządzany przez Spring.
- Wstrzykiwanie zależności przez konstruktor upraszcza łączenie komponentów i poprawia testowalność.
- `@Configuration` + `@Bean` pozwala definiować beany dla obiektów zewnętrznych.
- `application.properties` umożliwia zewnętrzną konfigurację; `@Value` wstrzykuje wartości.
- `@ImportResource` ładuje definicje beanów z plików XML.

---

## Zadanie 4: Migracja do Spring Boot (1 punkt)

### Kontekst

System zarządzania pracownikami wymaga refaktoryzacji do architektury Spring Boot z wykorzystaniem mechanizmu Dependency Injection. Dotychczasowa implementacja oparta na ręcznym tworzeniu obiektów i przekazywaniu zależności zostanie zastąpiona zarządzaniem przez kontener Spring. Dodatkowo system zostanie rozszerzony o możliwość definiowania pracowników bezpośrednio jako beany Spring w pliku konfiguracyjnym XML, co pozwoli poznać alternatywny sposób konfiguracji aplikacji Spring oprócz adnotacji.

### Wymagania funkcjonalne

1) Konfiguracja projektu Spring Boot

- Dodać do `pom.xml` lub `build.gradle` zależności Spring Boot 3.x: `spring-boot-starter`, `spring-boot-starter-test`. Zachować zależność `Gson`.
- Utworzyć `src/main/resources/application.properties` zawierający:
  - `app.api.url=https://jsonplaceholder.typicode.com/users`
  - `app.import.csv-file=employees.csv`
  - `logging.level.root=INFO`

2) Refaktoryzacja serwisów jako Spring Beany

- Oznaczyć `EmployeeService` adnotacją `@Service` (singleton bean) i usunąć ręczne tworzenie instancji.
- Oznaczyć `ImportService` adnotacją `@Service`; przyjmować `EmployeeService` przez konstruktor.
- Oznaczyć `ApiService` adnotacją `@Service`; przyjmować przez konstruktor `HttpClient` oraz `Gson` (beany z klasy konfiguracyjnej) oraz wstrzyknąć URL przez `@Value("${app.api.url}")`.

3) Definicja pracowników jako beany w pliku XML

Utworzyć `src/main/resources/employees-beans.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/util
           http://www.springframework.org/schema/util/spring-util.xsd">
    
    <bean id="employee1" class="com.techcorp.employee.model.Employee">
        <constructor-arg value="Jan"/>
        <constructor-arg value="Kowalski"/>
        <constructor-arg value="jan.kowalski@techcorp.com"/>
        <constructor-arg value="TechCorp"/>
        <constructor-arg value="MANAGER"/>
        <constructor-arg value="12500"/>
    </bean>
    
    <bean id="employee2" class="com.techcorp.employee.model.Employee">
        <constructor-arg value="Anna"/>
        <constructor-arg value="Nowak"/>
        <constructor-arg value="anna.nowak@techcorp.com"/>
        <constructor-arg value="TechCorp"/>
        <constructor-arg value="PROGRAMISTA"/>
        <constructor-arg value="8500"/>
    </bean>
    
    <!-- Definicja kolejnych pracowników -->
    
    <util:list id="xmlEmployees" value-type="com.techcorp.employee.model.Employee">
        <ref bean="employee1"/>
        <ref bean="employee2"/>
        <!-- Referencje do kolejnych beanów pracowników -->
    </util:list>
</beans>
```

Aby załadować konfigurację XML do Spring Boot, użyć `@ImportResource("classpath:employees-beans.xml")`.

4) Konfiguracja zewnętrznych zależności jako beany

Utworzyć klasę `AppConfig` (`@Configuration`) z metodami `@Bean` tworzącymi:

- `HttpClient httpClient()` zwracającą `HttpClient.newHttpClient()`
- `Gson gson()` zwracającą nową instancję `Gson`

5) Klasa startowa aplikacji

Utworzyć `EmployeeManagementApplication` (`@SpringBootApplication`, `@ImportResource("classpath:employees-beans.xml")`), implementującą `CommandLineRunner`. W `run` zademonstrować:

- import z CSV (`ImportService`),
- dodanie pracowników z beana `xmlEmployees` (przez `@Qualifier("xmlEmployees")` lub `@Resource(name = "xmlEmployees")`),
- pobranie danych z REST API przez `ApiService`,
- wyświetlenie statystyk (`EmployeeService`),
- walidację spójności wynagrodzeń i wskazanie underpaid.

---

## Struktura projektu

```text
project-root/
├── pom.xml (lub build.gradle)
├── src/
│   ├── main/
│   │   ├── java/com.techcorp.employee/
│   │   │   ├── EmployeeManagementApplication.java
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── model/
│   │   │   │   ├── Employee.java
│   │   │   │   ├── Position.java
│   │   │   │   ├── ImportSummary.java
│   │   │   │   └── CompanyStatistics.java
│   │   │   ├── service/
│   │   │   │   ├── EmployeeService.java
│   │   │   │   ├── ImportService.java
│   │   │   │   └── ApiService.java
│   │   │   └── exception/
│   │   │       ├── InvalidDataException.java
│   │   │       └── ApiException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── employees.csv
│   │       └── employees-beans.xml (plik z definicjami beanów)
│   └── test/
│       └── java/com.techcorp.employee/
│           └── service/
│               └── (testy z poprzedniego zadania bez zmian)
└── README.md
```

---

## Wymagania techniczne

- Wszystkie klasy serwisów muszą być zarządzane przez Spring Container jako beany. Nie używamy `new` dla klas oznaczonych `@Service`/`@Component`.
- Zależności wstrzykujemy wyłącznie przez konstruktor.
- Parametry konfiguracyjne pobieramy z `application.properties` przez `@Value`.
- Klasa `Employee` musi posiadać publiczny konstruktor z parametrami w kolejności zgodnej z XML.
- Bean `xmlEmployees` (typ `List<Employee>`) musi być dostępny do wstrzyknięcia przez `@Qualifier` lub `@Resource`.
- Aplikacja startuje komendą `mvn spring-boot:run` lub `gradle bootRun`. Testy z poprzedniego zadania mają działać bez zmian.

---

## Polecenia do uruchomienia

Maven:

```powershell
mvn spring-boot:run
mvn test
mvn package
```

Gradle:

```powershell
gradle bootRun
gradle test
gradle build
```

---

## Oddanie

Link do repozytorium z zaktualizowanym kodem zawierającym:

- `pom.xml` lub `build.gradle` z zależnościami Spring Boot,
- `application.properties` z parametrami konfiguracyjnymi,
- `employees-beans.xml` z definicjami beanów pracowników,
- przykładowy `employees.csv`,
- zaktualizowany `README` dokumentujący migrację (adnotacje, klasy konfiguracyjne, XML) oraz instrukcję uruchomienia i testowania.

---

## Kryteria recenzji

- Konfiguracja Spring Boot i adnotacje serwisów (25%)
  - Poprawne zależności w `pom.xml`/`build.gradle`. Serwisy oznaczone `@Service`, wstrzykiwanie przez konstruktor. `application.properties` zawiera wymagane parametry. Aplikacja poprawnie startuje.
- Konfiguracja beanów w klasie AppConfig (20%)
  - `@Configuration` z metodami `@Bean` tworzącymi `HttpClient` i `Gson`. Beany są poprawnie wstrzykiwane.
- Definicja pracowników jako beanów w XML (30%)
  - `EmployeeManagementApplication` ma `@SpringBootApplication` i `@ImportResource`, implementuje `CommandLineRunner`, demonstruje import CSV, wykorzystanie `xmlEmployees`, pobieranie z API i operacje analityczne.
- Klasa startowa i demonstracja funkcjonalności (25%)
  - Wszystkie zależności (w tym lista `xmlEmployees`) są wstrzykiwane przez konstruktor; demonstracja obejmuje pełny przepływ.
