import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import java.util.Scanner;

public class Main {
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String CATALOG_ID = "technologie";

    private static final String REPOSITORY_ID = "technologie";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "xyzzy";


    public static void main(String[] args) {
        AGRepositoryConnection connection = null;
        try {
            AGServer server = new AGServer(SERVER_URL, USERNAME, PASSWORD);
            AGRepository repository = server.createRepository(REPOSITORY_ID);

            connection = repository.getConnection();


            runMenu(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void runMenu(AGRepositoryConnection connection) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("Menu:");
            System.out.println("1. Dodaj zwierzę");
            System.out.println("2. Usuń zwierzę");
            System.out.println("3. Wyszukaj zwierzę po identyfikatorze");
            System.out.println("4. Wyszukaj zwierzę po gatunku");
            System.out.println("0. Wyjdź");
            System.out.print("Wybierz opcję: ");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addAnimal(connection, scanner);
                    break;
                case 2:
                    removeAnimal(connection, scanner);
                    break;
                case 3:
                    searchAnimalById(connection, scanner);
                    break;
                case 4:
                    searchAnimalBySpecies(connection, scanner);
                    break;
                case 0:
                    System.out.println("Koniec programu.");
                    break;
                default:
                    System.out.println("Nieprawidłowa opcja.");
                    break;
            }

            System.out.println();
        } while (choice != 0);

        scanner.close();
    }

    private static void addAnimal(AGRepositoryConnection connection, Scanner scanner) {
        System.out.println("Dodawanie zwierzęcia:");
        System.out.print("Podaj identyfikator zwierzęcia: ");
        String id = scanner.nextLine();
        System.out.print("Podaj nazwę zwierzęcia: ");
        String name = scanner.nextLine();
        System.out.print("Podaj gatunek zwierzęcia: ");
        String species = scanner.nextLine();

        ValueFactory valueFactory = connection.getValueFactory();
        String animalIRI = getAnimalIRI(id);

        connection.add(valueFactory.createIRI(animalIRI), valueFactory.createIRI("http://example.org/animal#name"), valueFactory.createLiteral(name));
        connection.add(valueFactory.createIRI(animalIRI), valueFactory.createIRI("http://example.org/animal#species"), valueFactory.createLiteral(species));

        System.out.println("Zwierzę zostało dodane.");
    }

    private static void removeAnimal(AGRepositoryConnection connection, Scanner scanner) {
        System.out.println("Usuwanie zwierzęcia:");
        System.out.print("Podaj identyfikator zwierzęcia do usunięcia: ");
        String id = scanner.nextLine();

        String animalIRI = getAnimalIRI(id);

        connection.remove(connection.getValueFactory().createIRI(animalIRI), null, null);

        System.out.println("Zwierzę zostało usunięte.");
    }

    private static void searchAnimalById(AGRepositoryConnection connection, Scanner scanner) {
        System.out.println("Wyszukiwanie zwierzęcia po identyfikatorze:");
        System.out.print("Podaj identyfikator zwierzęcia: ");
        String id = scanner.nextLine();

        String animalIRI = getAnimalIRI(id);

        String query = "SELECT ?name ?species WHERE { "
                + "<" + animalIRI + "> <http://example.org/animal#name> ?name ; "
                + "<http://example.org/animal#species> ?species . "
                + "}";

        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                System.out.println("Zwierzę o identyfikatorze " + id + " zostało znalezione.");
                while (result.hasNext()) {
                    String name = result.next().getValue("name").stringValue();
                    String species = result.next().getValue("species").stringValue();
                    System.out.println("Nazwa: " + name);
                    System.out.println("Gatunek: " + species);
                }
            } else {
                System.out.println("Nie znaleziono zwierzęcia o identyfikatorze " + id + ".");
            }
        }
    }

    private static void searchAnimalBySpecies(AGRepositoryConnection connection, Scanner scanner) {
        System.out.println("Wyszukiwanie zwierzęcia po gatunku:");
        System.out.print("Podaj gatunek zwierzęcia: ");
        String species = scanner.nextLine();

        String query = "SELECT ?name ?species WHERE { "
                + "?animal <http://example.org/animal#name> ?name ; "
                + "<http://example.org/animal#species> \"" + species + "\" . "
                + "}";

        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                System.out.println("Znaleziono zwierzęta o gatunku " + species + ":");
                while (result.hasNext()) {
                    String name = result.next().getValue("name").stringValue();
                    System.out.println("- " + name);
                }
            } else {
                System.out.println("Nie znaleziono zwierząt o gatunku " + species + ".");
            }
        }
    }

    private static String getAnimalIRI(String id) {
        return "http://example.org/animal#" + id;
    }

}