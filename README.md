# Przewodnik: Konfiguracja Maven/Gradle z JUnit 5 i JaCoCo

Poniższy przewodnik ilustruje konfigurację narzędzi buildowych, pisanie testów i mierzenie pokrycia kodu na przykładzie klasy Calculator.

## Kod do przetestowania

```java
// src/main/java/com/example/Calculator.java
package com.example;

public class Calculator {
    
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    public int multiply(int a, int b) {
        return a * b;
    }
    
    public double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return (double) a / b;
    }
}
```

**Wyjaśnienie:**

Klasa Calculator zawiera cztery podstawowe operacje matematyczne. Metoda divide zawiera warunek sprawdzający dzielenie przez zero - taka logika warunkowa jest szczególnie istotna z perspektywy testowania, ponieważ tworzy różne ścieżki wykonania kodu (gałęzie). Rzucanie wyjątku IllegalArgumentException wymaga osobnego testu sprawdzającego czy system poprawnie obsługuje błędne dane wejściowe.

## Opcja 1: Maven

### Konfiguracja pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>calculator</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Maven Surefire - uruchamia testy -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.2</version>
            </plugin>
            
            <!-- JaCoCo - pokrycie kodu -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

**Wyjaśnienie:**

Sekcja <properties> definiuje wersję Javy (17) oraz kodowanie znaków UTF-8. Sekcja <dependencies> zawiera JUnit Jupiter z zakresem test - oznacza to, że biblioteka będzie dostępna tylko podczas kompilacji i uruchamiania testów, nie w finalnej aplikacji.

Plugin Maven Surefire odpowiada za automatyczne wykrywanie i uruchamianie testów podczas fazy test w cyklu życia Mavena. Plugin JaCoCo jest skonfigurowany z dwoma executions:

- `prepare-agent` - instrumentuje kod przed uruchomieniem testów (dodaje specjalny kod śledzący wykonanie)
- `report` - po wykonaniu testów generuje raport HTML z informacją o pokryciu

### Komendy Maven

```bash
mvn clean test              # Kompilacja i uruchomienie testów
mvn jacoco:report          # Raport pokrycia w target/site/jacoco/index.html
mvn clean verify           # Pełny cykl życia z weryfikacją
```

**Wyjaśnienie:**

Komenda mvn clean test usuwa poprzednie artefakty buildu (clean) i wykonuje wszystkie fazy do test włącznie - kompiluje kod źródłowy, kompiluje testy i uruchamia je. Komenda mvn jacoco:report bezpośrednio wywołuje goal raportu JaCoCo. Komenda mvn clean verify wykonuje kompletny cykl życia włącznie z weryfikacją jakości kodu.

## Opcja 2: Gradle

### Konfiguracja build.gradle

```groovy
plugins {
    id 'java'
    id 'jacoco'
}

group = 'com.example'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    dependsOn test
    reports {
        html.required = true
        xml.required = false
        csv.required = false
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

**Wyjaśnienie:**

Gradle używa DSL (Domain Specific Language) opartego na Groovy zamiast XML. Sekcja plugins aktywuje obsługę Javy i JaCoCo. Repozytorium mavenCentral() wskazuje skąd pobierać zależności.

Konfiguracja testImplementation to odpowiednik Maven'owego <scope>test</scope>. Instrukcja useJUnitPlatform() mówi Gradle, aby używał JUnit 5 (nie starszego JUnit 4). Polecenie finalizedBy jacocoTestReport powoduje automatyczne generowanie raportu pokrycia po każdym uruchomieniu testów.

Blok jacocoTestReport konfiguruje format raportów - w tym przypadku tylko HTML, bez XML i CSV.

### Komendy Gradle

```bash
./gradlew clean test           # Kompilacja i uruchomienie testów
./gradlew jacocoTestReport     # Raport w build/reports/jacoco/test/html/index.html
./gradlew build                # Pełny build projektu
```

**Wyjaśnienie:**

Gradle używa wrappera (gradlew) zapewniającego spójną wersję narzędzia. Komenda clean test działa analogicznie do Mavena. Dzięki konfiguracji finalizedBy, raport JaCoCo generuje się automatycznie po test, ale można go też wywołać ręcznie. Komenda build wykonuje pełny cykl budowania włącznie z tworzeniem JAR.

## Testy JUnit 5

```java
// src/test/java/com/example/CalculatorTest.java
package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {
    
    private Calculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }
    
    @Test
    @DisplayName("Dodawanie dwóch liczb dodatnich")
    void shouldAddTwoPositiveNumbers() {
        // Arrange
        int a = 5;
        int b = 3;
        
        // Act
        int result = calculator.add(a, b);
        
        // Assert
        assertEquals(8, result);
    }
    
    @Test
    void shouldSubtractNumbers() {
        assertEquals(2, calculator.subtract(5, 3));
    }
    
    @ParameterizedTest
    @CsvSource({
        "2, 3, 6",
        "5, 4, 20",
        "-2, 3, -6"
    })
    void shouldMultiplyNumbers(int a, int b, int expected) {
        assertEquals(expected, calculator.multiply(a, b));
    }
    
    @Test
    void shouldDivideNumbers() {
        assertEquals(2.5, calculator.divide(5, 2), 0.001);
    }
    
    @Test
    void shouldThrowExceptionWhenDividingByZero() {
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> calculator.divide(10, 0)
        );
        
        assertEquals("Division by zero", exception.getMessage());
    }
}
```

**Wyjaśnienie:**

Metoda setUp() z adnotacją @BeforeEach tworzy nową instancję kalkulatora przed każdym testem - zapewnia to izolację testów, ponieważ każdy test pracuje na świeżym obiekcie.

Test shouldAddTwoPositiveNumbers() demonstruje wzorzec AAA (Arrange-Act-Assert): najpierw przygotowanie danych, wykonanie operacji, weryfikacja wyniku. Adnotacja @DisplayName pozwala nadać czytelną nazwę widoczną w raportach.

Test shouldMultiplyNumbers() używa @ParameterizedTest - jeden test wykonuje się trzykrotnie z różnymi zestawami danych. Wartość @CsvSource definiuje dane wejściowe w formacie: "parametr1, parametr2, oczekiwanyWynik". Takie podejście redukuje duplikację kodu testowego.

Test shouldDivideNumbers() używa trzeciego parametru w assertEquals (0.001) jako tolerancji dla liczb zmiennoprzecinkowych - porównywanie double wymaga marginesu błędu ze względu na niedokładności reprezentacji.

Test shouldThrowExceptionWhenDividingByZero() sprawdza scenariusz negatywny. Metoda assertThrows przyjmuje klasę wyjątku i wyrażenie lambda, które powinno go rzucić. Następnie weryfikowana jest treść komunikatu błędu.

## Kluczowe koncepcje

### Maven vs Gradle

| Aspekt | Maven | Gradle |
|--------|-------|--------|
| Format | XML | Groovy/Kotlin DSL |
| Konwencja | Sztywna struktura | Elastyczna konfiguracja |
| Wydajność | Wolniejszy | Szybszy (incremental builds) |
| Krzywa uczenia | Łatwiejszy start | Bardziej złożony |

Maven opiera się na konwencji "convention over configuration" - jeśli kod znajduje się w standardowych lokalizacjach, większość konfiguracji działa automatycznie. Cykl życia (clean, compile, test, package, verify, install, deploy) jest sztywno zdefiniowany.

Gradle oferuje większą elastyczność i lepszą wydajność dzięki mechanizmom takim jak incremental compilation (kompilacja tylko zmienionych plików) i build cache. Format DSL jest bardziej zwięzły niż XML, ale wymaga znajomości Groovy/Kotlin.

### Adnotacje JUnit 5

**Podstawowe adnotacje:**

- `@Test` - oznacza metodę testową wykrywaną i uruchamianą przez framework
- `@BeforeEach` - metoda wykonująca się przed każdym testem (np. inicjalizacja obiektów)
- `@AfterEach` - metoda wykonująca się po każdym teście (np. czyszczenie zasobów)
- `@BeforeAll` - metoda wykonująca się raz przed wszystkimi testami w klasie (musi być static)
- `@AfterAll` - metoda wykonująca się raz po wszystkich testach w klasie (musi być static)
- `@DisplayName` - czytelna nazwa testu wyświetlana w raportach i IDE

**Testy parametryzowane:**

- `@ParameterizedTest` - test wykonujący się wielokrotnie z różnymi danymi wejściowymi
- `@CsvSource` - dane w formacie CSV bezpośrednio w adnotacji
- `@ValueSource` - proste wartości jednego typu (int, String, etc.)
- `@MethodSource` - dane pochodzące z metody w klasie testowej

**Kontrola wykonania:**

- `@Disabled` - wyłącza test z wykonania (z opcjonalnym powodem)
- `@Tag` - oznacza test tagiem do selektywnego uruchamiania grup testów

### Struktura testu (AAA)

Każdy dobrze napisany test powinien mieć wyraźną, trójczęściową strukturę:

- **Arrange (Przygotowanie)** - inicjalizacja obiektów testowych, przygotowanie danych wejściowych, konfiguracja mocków. Ta sekcja odpowiada na pytanie: "Co potrzebuję, aby wykonać test?"
- **Act (Działanie)** - wykonanie jednej konkretnej operacji będącej przedmiotem testu. Zazwyczaj to wywołanie jednej metody. Ta sekcja powinna być najkrótsza.
- **Assert (Sprawdzenie)** - weryfikacja wyniku za pomocą asercji. Sprawdzenie czy wynik działania jest zgodny z oczekiwaniami. Może zawierać kilka asercji weryfikujących różne aspekty wyniku.

Struktura AAA zwiększa czytelność testów i ułatwia ich utrzymanie. Separacja fizyczna (puste linie) między sekcjami lub komentarze // Arrange, // Act, // Assert dodatkowo poprawiają zrozumiałość.

### JaCoCo - metryki pokrycia
JaCoCo (Java Code Coverage) instrumentuje bytecode podczas wykonania testów, śledząc które instrukcje zostały wykonane. Generuje raporty w kilku formatach.

**Podstawowe metryki:**

- **Lines (Linie)** - procent wykonanych linii kodu. Najprostsza metryka, ale nie uwzględnia logiki warunkowej. Linia zawierająca if jest "pokryta" nawet jeśli sprawdzono tylko jeden przypadek.
- **Branches (Gałęzie)** - procent sprawdzonych warunków logicznych (if/else, switch, pętle, operatory warunkowe). Metryka ta pokazuje czy przetestowano wszystkie możliwe ścieżki przez kod. Dla instrukcji if (x > 0) pełne pokrycie gałęzi wymaga testów dla obu przypadków: true i false.
- **Instructions (Instrukcje)** - pojedyncze instrukcje bytecode. Bardziej szczegółowa metryka niż linie, niezależna od formatowania kodu.
- **Methods (Metody)** - procent metod w których wykonano przynajmniej jedną instrukcję.
- **Classes (Klasy)** - procent klas zawierających przynajmniej jedną wykonaną metodę.
- **Complexity (Złożoność cyklomatyczna)** - liczba niezależnych ścieżek przez kod. Wysoka złożoność sugeruje potrzebę większej liczby testów.

**Interpretacja raportu HTML:**

Raport JaCoCo używa kolorowego kodowania:

- 🟢 Zielony - kod w pełni pokryty testami
- 🔴 Czerwony - kod niepokryty testami
- 🟡 Żółty - częściowo pokryte gałęzie warunkowe (np. tylko case true, bez case false)

Liczniki przy każdej klasie/metodzie pokazują ułamek: licznik wykonanych/całkowita liczba. Na przykład "5/8" przy branżach oznacza, że 5 z 8 gałęzi zostało przetestowanych.

**Uwaga:** Wysokie pokrycie nie gwarantuje jakości testów. Test może wykonać kod bez sprawdzania poprawności wyniku. Ważniejsza od samego procentu jest jakość scenariuszy testowych i asercji.

### Struktura katalogów

```
calculator/
├── pom.xml (lub build.gradle)
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/
│   │           └── Calculator.java
│   └── test/
│       └── java/
│           └── com/example/
│               └── CalculatorTest.java
└── target/ (Maven) lub build/ (Gradle)
    └── site/jacoco/index.html
```

**Wyjaśnienie:**

Maven i Gradle używają konwencji rozdzielenia kodu produkcyjnego (src/main/java) od testowego (src/test/java). Struktura pakietów w obu katalogach powinna być identyczna - test dla klasy com.example.Calculator znajduje się w com.example.CalculatorTest.

Katalog target/ (Maven) lub build/ (Gradle) zawiera wygenerowane artefakty: skompilowane klasy, JARy, raporty. Raport JaCoCo znajduje się w podkatalogu site/jacoco/ (Maven) lub reports/jacoco/test/html/ (Gradle). Plik index.html to punkt wejścia do interaktywnego raportu HTML.

## Dobre praktyki

1. **Jeden test = jedna odpowiedzialność** - test sprawdza konkretny scenariusz lub jedno zachowanie. Unika się testowania wielu niezależnych funkcjonalności w jednym teście. Ułatwia to diagnozę problemów - gdy test failuje, od razu wiadomo co jest zepsute.
2. **Nazwy opisowe** - nazwa metody testowej wyraźnie komunikuje co testuje i jaki jest oczekiwany rezultat. Konwencje: shouldDoX_whenY(), givenX_whenY_thenZ(), lub po prostu opisowe zdanie bez podziałów. Dobra nazwa eliminuje potrzebę czytania implementacji testu, aby zrozumieć jego cel.
3. **Izolacja** - testy nie zależą od siebie nawzajem i nie dzielą stanu. Kolejność wykonania testów nie wpływa na wyniki. Każdy test przygotowuje swoje dane (lub używa @BeforeEach) i nie modyfikuje globalnego stanu. Pozwala to uruchamiać testy równolegle i debugować je niezależnie.
4. **Powtarzalność** - test zawsze daje ten sam wynik przy tych samych warunkach. Unikać zależności od daty systemowej, losowości, kolejności elementów w kolekcjach bez gwarantowanego porządku (HashSet), zewnętrznych serwisów. Test, który czasem przechodzi a czasem failuje (flaky test) traci wartość.
5. **Szybkość** - testy jednostkowe powinny wykonywać się w milisekundach. Szybkie testy można uruchamiać często podczas development, co skraca pętlę feedbacku. Operacje I/O, dostęp do bazy danych, żądania sieciowe spowalniają testy - w testach jednostkowych należy je mockować.
6. **Kompletność scenariuszy** - testować happy path, edge cases i error cases. Dla metody przyjmującej liczby: wartości dodatnie, ujemne, zero, wartości graniczne (Integer.MAX_VALUE). Dla kolekcji: puste, jeden element, wiele elementów, null.
7. **Pokrycie kodu to wskaźnik pomocniczy, nie cel sam w sobie.** 100% pokrycia linii nie oznacza przetestowania wszystkich scenariuszy - logika może być błędna mimo wykonania każdej linii. Ważniejsze jest przemyślenie jakie scenariusze mogą wystąpić w produkcji i czy są one przetestowane.

---
---

**Przykład jest do pobrania pod adresem:** https://github.com/MateuszMiotkCodeExamples/AplikacjePrzemyslowe/tree/maven-gradle-junit-jacoco-example

---

# Zadanie 3: Testy jednostkowe i automatyzacja buildu

## Kontekst

System zarządzania pracownikami wymaga profesjonalnego podejścia do jakości kodu. Zadaniem jest przygotowanie testów jednostkowych oraz skonfigurowanie wybranego narzędzia buildowego (Maven LUB Gradle) z raportowaniem pokrycia kodu.

## Cele edukacyjne

Po wykonaniu tego zadania oczekiwane umiejętności obejmują:

- Konfigurację projektu z JUnit 5 w wybranym narzędziu buildowym
- Pisanie efektywnych testów jednostkowych z użyciem mockowania
- Generowanie i interpretację raportów pokrycia kodu
- Uruchamianie testów i budowanie projektu z linii komend

## Wymagania funkcjonalne

### 1. Wybór i konfiguracja narzędzia buildowego

Należy wybrać JEDNO narzędzie: Maven LUB Gradle

Wymagana konfiguracja obejmuje:

- JUnit 5 (Jupiter) - najnowszą stabilną wersję
- Mockito z integracją JUnit 5
- Plugin do uruchamiania testów
- Plugin JaCoCo do raportowania pokrycia kodu

Aspekty do rozważenia:

- Różnice między JUnit 4 a JUnit 5 w kontekście konfiguracji
- Rola dedykowanego pluginu do uruchamiania testów
- Mechanizm działania JaCoCo w kontekście śledzenia wykonanych linii kodu

### 2. Testy dla EmployeeService

Analiza wymagana przed implementacją:

- Identyfikacja najbardziej krytycznych metod w EmployeeService
- Określenie potencjalnych problemów w każdej z tych metod
- Rozpoznanie danych wejściowych mogących spowodować nieoczekiwane zachowanie

Scenariusze wymagające pokrycia testami:

- Dodawanie pracownika - obsługa powtarzających się emaili i wartości null
- Wyszukiwanie po firmie - zachowanie przy nieistniejącej firmie
- Średnie wynagrodzenie - obsługa pustej listy pracowników
- Maksymalne wynagrodzenie - typ zwracany i jego znaczenie przy pustej liście
- Walidacja wynagrodzeń - identyfikacja nieprawidłowości

Zagadnienia projektowe:

- Konwencja nazewnictwa metod testowych zapewniająca czytelność w długim okresie
- Przygotowanie danych testowych równoważące czytelność i kompletność
- Zasadność użycia metody @BeforeEach do inicjalizacji wspólnych danych

### 3. Testy dla ImportService

Kluczowe wyzwanie: Testowanie operacji na plikach

Aspekty wymagające uwagi:

- Podejście do tworzenia plików CSV dla celów testowych
- Zapewnienie przenośności testów między różnymi systemami operacyjnymi
- Gwarancja izolacji testów i braku wzajemnych zależności

Scenariusze do implementacji:

- Poprawny import - weryfikacja trafiania danych do systemu
- Niepoprawne stanowisko - decyzja o przerwaniu lub kontynuacji importu
- Ujemne wynagrodzenie - określenie sposobu obsługi przez system
- Weryfikacja podsumowania - zawartość obiektu ImportSummary

**Wskazówka techniczna:** JUnit 5 udostępnia mechanizm do pracy z tymczasowymi zasobami. Dokumentacja frameworka zawiera informacje o rozwiązaniach dotyczących katalogów tymczasowych.

### 4. Testy dla ApiService z mockowaniem

Fundamentalne zagadnienie: Uzasadnienie unikania prawdziwych żądań HTTP w testach jednostkowych

Punkty do analizy:

- Konsekwencje niedostępności API podczas uruchamiania testów
- Możliwość testowania obsługi błędów przy zawsze działającym API
- Zależność testów od zewnętrznych serwisów i jej implikacje

Koncepcja mockowania:

- Definicja mock object i różnice względem prawdziwego obiektu
- Identyfikacja obiektów w ApiService wymagających mockowania
- Mechanizm konfiguracji wartości zwracanych przez mocki

Scenariusze:

- Poprawna odpowiedź JSON - symulacja odpowiedzi bez prawdziwego API
- Błąd HTTP (404, 500) - weryfikacja rzucania wyjątków
- Parsowanie danych - poprawność mapowania z JSON do Employee

**Aspekt weryfikacji:** Mockito oferuje różne sposoby weryfikacji - analiza czy wystarczające jest sprawdzenie wartości zwracanej, czy konieczna jest również weryfikacja wywołań konkretnych metod.

### 5. Raportowanie pokrycia kodu z JaCoCo

Cel: Minimum 70% pokrycia dla pakietu service

Zagadnienia teoretyczne:

- Znaczenie metryki "70% pokrycia linii"
- Relacja między pokryciem 100% a idealnymi testami
- Różnice między pokryciem linii, gałęzi i ścieżek

Konfiguracja do ustalenia:

- Lokalizacja generowania raportu przez plugin JaCoCo
- Format raportu (HTML, XML, CSV)
- Automatyzacja generowania raportu po wykonaniu testów

## Struktura projektu

```
project-root/
├── pom.xml (lub build.gradle)
├── src/
│   ├── main/java/...
│   └── test/
│       ├── java/
│       │   └── service/
│       │       ├── EmployeeServiceTest.java
│       │       ├── ImportServiceTest.java
│       │       └── ApiServiceTest.java
│       └── resources/
│           └── (opcjonalne pliki testowe)
└── README.md
```

## Polecenia do uruchomienia

Należy ustalić standardowe komendy dla wybranego narzędzia obejmujące:

- Uruchamianie wyłącznie testów
- Uruchamianie testów z jednoczesnym generowaniem raportu pokrycia
- Uruchamianie pełnego cyklu budowania z weryfikacją

## Kryteria recenzji:

- **Poprawna konfiguracja narzędzia buildowego + JUnit 5 + JaCoCo (25%)**
- **Testy EmployeeService (30%)**
- **Testy ImportService w tym obsługa błędów (20%)**
- **Testy ApiService z użyciem mocków (15%)**
- **Pokrycie ≥70% (10%)**

---

Twoja odpowiedź zostanie oceniona przez autora kursu na podstawie powyższych kryteriów. Końcowa ocena będzie przyznana po zakończeniu recenzji.