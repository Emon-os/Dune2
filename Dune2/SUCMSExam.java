
import java.util.*;

public class SUCMSExam {

    // Utility Method
    static void requireText(String s) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("Blank value");
    }

    // 1. Singleton Pattern
    static final class UniversityConfig {

        private static final UniversityConfig INSTANCE =
                new UniversityConfig();

        private final int maxCredits = 6;

        private UniversityConfig() { }

        public static UniversityConfig getInstance() {
            return INSTANCE;
        }

        public int maxCredits() {
            return maxCredits;
        }
    }

    // 2. Factory Pattern
    interface UserAccount {
        String id();
        String role();
    }

    static final class Student implements UserAccount {

        private final String id;

        Student(String id) {
            requireText(id);
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String role() {
            return "STUDENT";
        }
    }

    static final class Faculty implements UserAccount {

        private final String id;

        Faculty(String id) {
            requireText(id);
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String role() {
            return "FACULTY";
        }
    }

    static final class Administrator implements UserAccount {

        private final String id;

        Administrator(String id) {
            requireText(id);
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String role() {
            return "ADMIN";
        }
    }

    static final class AccountFactory {

        public static UserAccount create(String role, String id) {

            if ("STUDENT".equals(role))
                return new Student(id);

            if ("FACULTY".equals(role))
                return new Faculty(id);

            if ("ADMIN".equals(role))
                return new Administrator(id);

            throw new IllegalArgumentException(
                    "Unsupported role");
        }
    }

    // 3. Course Entity
    static final class Course {

        final String code;
        final int credits, capacity;

        private final Set<String> students =
                new HashSet<>();

        Course(String code, int credits, int capacity) {

            requireText(code);

            if (credits <= 0 || capacity <= 0)
                throw new IllegalArgumentException();

            this.code = code;
            this.credits = credits;
            this.capacity = capacity;
        }
    }

    // 4. Course Registration Manager
    static final class CourseRegistrationManager {

        private final Map<String, Set<String>> enrolled =
                new HashMap<>();

        private final Map<String, Integer> creditTotals =
                new HashMap<>();

        public void register(Student s, Course c) {

            Set<String> codes = enrolled.getOrDefault(
                    s.id(), Collections.emptySet());

            int current = creditTotals.getOrDefault(
                    s.id(), 0);

            if (codes.contains(c.code))
                throw new IllegalStateException(
                        "Duplicate course");

            if (c.students.size() >= c.capacity)
                throw new IllegalStateException(
                        "Full course");

            if (current + c.credits >
                    UniversityConfig.getInstance().maxCredits())
                throw new IllegalStateException(
                        "Credit limit");

            enrolled.computeIfAbsent(
                    s.id(), key -> new HashSet<>())
                    .add(c.code);

            c.students.add(s.id());

            creditTotals.put(
                    s.id(), current + c.credits);
        }

        public int credits(Student s) {
            return creditTotals.getOrDefault(
                    s.id(), 0);
        }
    }

    // 5. Observer Pattern
    interface CampusObserver {
        void update(String recipient, String message);
    }

    static final class StudentPortal
            implements CampusObserver {

        private final String studentId;
        int received;

        StudentPortal(String id) {
            studentId = id;
        }

        public void update(String recipient, String message) {

            if (recipient != null &&
                    !recipient.equals(studentId))
                return;

            received++;

            System.out.println(
                    "Student " + studentId + ": " + message);
        }
    }

    static final class FacultyPortal
            implements CampusObserver {

        int received;

        public void update(String recipient, String message) {

            if (recipient != null)
                return;

            received++;

            System.out.println(
                    "Faculty: " + message);
        }
    }

    static final class NoticePublisher {

        private final List<CampusObserver> observers =
                new ArrayList<>();

        public void subscribe(CampusObserver o) {

            if (!observers.contains(o))
                observers.add(o);
        }

        public void unsubscribe(CampusObserver o) {
            observers.remove(o);
        }

        public void publish(String recipient, String message) {

            for (CampusObserver o :
                    new ArrayList<>(observers)) {

                o.update(recipient, message);
            }
        }
    }

    // 6. Marks Entity
    static final class Marks {

        final double exam, coursework, project;

        Marks(double exam, double coursework, double project) {

            for (double x :
                    new double[]{exam, coursework, project}) {

                if (!Double.isFinite(x) || x < 0 || x > 100)
                    throw new IllegalArgumentException(
                            "Marks must be 0 to 100");
            }

            this.exam = exam;
            this.coursework = coursework;
            this.project = project;
        }
    }

    // 7. Strategy Pattern
    interface EvaluationStrategy {
        double evaluate(Marks m);
    }

    static final class TheoryEvaluation
            implements EvaluationStrategy {

        public double evaluate(Marks m) {
            return 0.6 * m.exam + 0.4 * m.coursework;
        }
    }

    static final class LabEvaluation
            implements EvaluationStrategy {

        public double evaluate(Marks m) {

            return 0.3 * m.exam
                    + 0.2 * m.coursework
                    + 0.5 * m.project;
        }
    }

    // Evaluation Service
    static final class EvaluationService {

        private EvaluationStrategy strategy;
        private final NoticePublisher publisher;

        EvaluationService(
                EvaluationStrategy s,
                NoticePublisher p) {

            strategy = s;
            publisher = p;
        }

        public void setStrategy(EvaluationStrategy s) {
            strategy = s;
        }

        public double publishResult(Student student, Marks marks) {

            double score = strategy.evaluate(marks);

            publisher.publish(
                    student.id(), "Score=" + score);

            return score;
        }
    }

    // 8. Adapter Pattern
    interface LibraryService {
        boolean isAvailable(String isbn);
    }

    static final class ExternalLibrarySystem {

        public String copiesAsText(String isbn) {

            if ("BOOK1".equals(isbn))
                return "2";

            if ("BOOK2".equals(isbn))
                return "0";

            return "UNKNOWN";
        }
    }

    static final class LibraryAdapter
            implements LibraryService {

        private final ExternalLibrarySystem external;

        LibraryAdapter(ExternalLibrarySystem external) {
            this.external = external;
        }

        public boolean isAvailable(String isbn) {

            requireText(isbn);

            String text = external.copiesAsText(isbn);

            if ("UNKNOWN".equals(text))
                throw new IllegalArgumentException(
                        "Unknown book");

            int copies = Integer.parseInt(text);

            if (copies < 0)
                throw new IllegalArgumentException(
                        "Invalid copies");

            return copies > 0;
        }
    }

    // 9. Testing Methods
    static void expect(boolean ok, String label) {

        if (!ok)
            throw new AssertionError(label);

        System.out.println("PASS: " + label);
    }

    static void rejects(Runnable action, String label) {

        try {
            action.run();

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            System.out.println("PASS: " + label);
            return;
        }

        throw new AssertionError(label);
    }

    // 10. Main Method
    public static void main(String[] args) {

        // Singleton Testing
        expect(
                UniversityConfig.getInstance() ==
                UniversityConfig.getInstance(),
                "singleton");

        // Factory Testing
        Student student = (Student)
                AccountFactory.create("STUDENT", "S1");

        Student second = (Student)
                AccountFactory.create("STUDENT", "S2");

        expect(
                AccountFactory.create("FACULTY", "F1")
                        instanceof Faculty,
                "faculty factory");

        expect(
                AccountFactory.create("ADMIN", "A1")
                        instanceof Administrator,
                "admin factory");

        rejects(
                () -> AccountFactory.create("UNKNOWN", "X"),
                "unsupported account");

        rejects(
                () -> AccountFactory.create("STUDENT", ""),
                "blank account ID");

        // Course Registration Testing
        CourseRegistrationManager manager =
                new CourseRegistrationManager();

        Course c1 = new Course("C1", 3, 1);
        Course c2 = new Course("C2", 3, 5);
        Course c3 = new Course("C3", 3, 5);

        manager.register(student, c1);

        rejects(
                () -> manager.register(student, c1),
                "duplicate registration");

        rejects(
                () -> manager.register(second, c1),
                "capacity limit");

        manager.register(student, c2);

        expect(
                manager.credits(student) == 6,
                "six credit boundary accepted");

        rejects(
                () -> manager.register(student, c3),
                "credit limit exceeded");

        expect(
                manager.credits(student) == 6 &&
                manager.credits(second) == 0,
                "failed registration leaves credits unchanged");

        // Observer Testing
        NoticePublisher publisher =
                new NoticePublisher();

        StudentPortal p1 = new StudentPortal("S1");
        StudentPortal p2 = new StudentPortal("S2");

        FacultyPortal faculty = new FacultyPortal();

        publisher.subscribe(p1);
        publisher.subscribe(p2);
        publisher.subscribe(faculty);

        publisher.publish(null, "Library hours updated");

        expect(
                p1.received == 1 &&
                p2.received == 1 &&
                faculty.received == 1,
                "broadcast notice");

        // Strategy Testing
        EvaluationService evaluation =
                new EvaluationService(
                        new TheoryEvaluation(), publisher);

        Marks m = new Marks(80, 70, 90);

        expect(
                Math.abs(
                        evaluation.publishResult(student, m) - 76
                ) < 1e-9,
                "theory score 76");

        expect(
                p1.received == 2 &&
                p2.received == 1 &&
                faculty.received == 1,
                "result recipient filtering");

        evaluation.setStrategy(new LabEvaluation());

        expect(
                Math.abs(
                        evaluation.publishResult(student, m) - 83
                ) < 1e-9,
                "lab score 83");

        // Observer Unsubscription
        publisher.unsubscribe(p2);

        publisher.publish(null, "Campus notice");

        expect(
                p2.received == 1 &&
                p1.received == 4 &&
                faculty.received == 2,
                "observer removal");

        // Invalid Marks Testing
        rejects(
                () -> new Marks(-1, 70, 90),
                "negative marks rejected");

        rejects(
                () -> new Marks(101, 70, 90),
                "marks above 100 rejected");

        rejects(
                () -> new Marks(Double.NaN, 70, 90),
                "NaN marks rejected");

        // Adapter Testing
        LibraryService library =
                new LibraryAdapter(
                        new ExternalLibrarySystem());

        expect(
                library.isAvailable("BOOK1"),
                "available book");

        expect(
                !library.isAvailable("BOOK2"),
                "unavailable book");

        rejects(
                () -> library.isAvailable("MISSING"),
                "unknown book");

        System.out.println("All checks completed.");
    }
}
