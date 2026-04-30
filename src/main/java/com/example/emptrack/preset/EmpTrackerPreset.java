package com.example.emptrack.preset;

import com.example.emptrack.model.Department;
import com.example.emptrack.model.Employee;
import com.example.emptrack.model.User;
import com.example.emptrack.repository.DepartmentRepository;
import com.example.emptrack.repository.EmployeeRepository;
import com.example.emptrack.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
public class EmpTrackerPreset {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public EmpTrackerPreset(UserRepository userRepository,
                                    DepartmentRepository departmentRepository,
                                    EmployeeRepository employeeRepository,
                                    BCryptPasswordEncoder passwordEncoder,
                                    JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String reseedDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE employee");
        jdbcTemplate.execute("TRUNCATE TABLE department");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        return seedDatabase();
    }

    private String seedDatabase() {

        // ───── USERS ─────
        User admin = new User();
        admin.setFirstName("Test");
        admin.setLastName("Admin");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        admin.setActive(true);

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.USER);
        user.setActive(true);

        userRepository.saveAll(List.of(admin, user));

        // ───── DEPARTMENTS ─────
        Department engineering = new Department();
        engineering.setName("Software Engineering");
        engineering.setActive(true);

        Department qa = new Department();
        qa.setName("Quality Assurance");
        qa.setActive(true);

        Department devops = new Department();
        devops.setName("DevOps & Infrastructure");
        devops.setActive(true);

        Department hr = new Department();
        hr.setName("Human Resources");
        hr.setActive(true);

        Department finance = new Department();
        finance.setName("Finance & Accounting");
        finance.setActive(true);

        departmentRepository.saveAll(List.of(engineering, qa, devops, hr, finance));

        // ───── EMPLOYEES ─────

        // Software Engineering (12 employees) — ages 24–45, salaries 55k–120k
        Employee emp1  = createEmployee("Liam",      "Torres",     LocalDate.of(1990, 3, 15),  engineering, "85000");
        Employee emp2  = createEmployee("Sofia",     "Reyes",      LocalDate.of(1993, 7, 22),  engineering, "80000");
        Employee emp3  = createEmployee("Marcus",    "Chan",       LocalDate.of(1980, 11, 8),  engineering, "115000");
        Employee emp4  = createEmployee("Aria",      "Mendoza",    LocalDate.of(2000, 4, 30),  engineering, "58000");
        Employee emp5  = createEmployee("Ethan",     "Park",       LocalDate.of(1992, 9, 14),  engineering, "82000");
        Employee emp6  = createEmployee("Isabella",  "Santos",     LocalDate.of(1985, 6, 25),  engineering, "105000");
        Employee emp7  = createEmployee("Noah",      "Cruz",       LocalDate.of(1998, 1, 17),  engineering, "62000");
        Employee emp8  = createEmployee("Mia",       "Villanueva", LocalDate.of(1983, 12, 3),  engineering, "118000");
        Employee emp9  = createEmployee("Adrian",    "Reyes",      LocalDate.of(2001, 5, 10),  engineering, "55000");
        Employee emp10 = createEmployee("Camille",   "Lim",        LocalDate.of(1996, 8, 19),  engineering, "72000");
        Employee emp11 = createEmployee("Patrick",   "Uy",         LocalDate.of(1978, 2, 14),  engineering, "120000");
        Employee emp12 = createEmployee("Danielle",  "Sy",         LocalDate.of(1994, 11, 27), engineering, "79000");

        // Quality Assurance (10 employees) — ages 22–42, salaries 40k–85k
        Employee emp13 = createEmployee("James",     "Lim",        LocalDate.of(1991, 5, 20),  qa, "65000");
        Employee emp14 = createEmployee("Emma",      "Tan",        LocalDate.of(1999, 8, 11),  qa, "45000");
        Employee emp15 = createEmployee("Carlos",    "Dela Cruz",  LocalDate.of(1982, 2, 28),  qa, "82000");
        Employee emp16 = createEmployee("Olivia",    "Garcia",     LocalDate.of(2002, 10, 5),  qa, "40000");
        Employee emp17 = createEmployee("Lucas",     "Ramos",      LocalDate.of(1993, 3, 19),  qa, "64000");
        Employee emp18 = createEmployee("Ava",       "Florendo",   LocalDate.of(1990, 7, 7),   qa, "67000");
        Employee emp19 = createEmployee("Miguel",    "Santos",     LocalDate.of(1997, 4, 23),  qa, "52000");
        Employee emp20 = createEmployee("Hannah",    "Bautista",   LocalDate.of(1984, 9, 12),  qa, "78000");
        Employee emp21 = createEmployee("Rafael",    "Aquino",     LocalDate.of(2000, 1, 30),  qa, "43000");
        Employee emp22 = createEmployee("Trisha",    "Navarro",    LocalDate.of(1995, 6, 14),  qa, "60000");

        // DevOps & Infrastructure (10 employees) — ages 26–50, salaries 75k–140k
        Employee emp23 = createEmployee("Benjamin",  "Ocampo",     LocalDate.of(1988, 4, 14),  devops, "92000");
        Employee emp24 = createEmployee("Charlotte", "Bautista",   LocalDate.of(1992, 11, 23), devops, "89000");
        Employee emp25 = createEmployee("Henry",     "Aquino",     LocalDate.of(1975, 6, 9),   devops, "138000");
        Employee emp26 = createEmployee("Amelia",    "Pascual",    LocalDate.of(1998, 1, 31),  devops, "78000");
        Employee emp27 = createEmployee("Alexander", "Navarro",    LocalDate.of(1973, 9, 16),  devops, "140000");
        Employee emp28 = createEmployee("Harper",    "Valdez",     LocalDate.of(1993, 5, 4),   devops, "87000");
        Employee emp29 = createEmployee("Jerome",    "Castillo",   LocalDate.of(1986, 7, 22),  devops, "110000");
        Employee emp30 = createEmployee("Kristine",  "Morales",    LocalDate.of(2000, 3, 8),   devops, "75000");
        Employee emp31 = createEmployee("Vincent",   "Domingo",    LocalDate.of(1979, 10, 19), devops, "125000");
        Employee emp32 = createEmployee("Patricia",  "Aguilar",    LocalDate.of(1996, 12, 5),  devops, "84000");

        // Human Resources (9 employees) — ages 23–48, salaries 35k–75k
        Employee emp33 = createEmployee("Evelyn",    "Castillo",   LocalDate.of(1989, 8, 27),  hr, "58000");
        Employee emp34 = createEmployee("Daniel",    "Morales",    LocalDate.of(1999, 3, 13),  hr, "38000");
        Employee emp35 = createEmployee("Scarlett",  "Domingo",    LocalDate.of(1991, 12, 22), hr, "57000");
        Employee emp36 = createEmployee("Michael",   "Aguilar",    LocalDate.of(1977, 6, 18),  hr, "72000");
        Employee emp37 = createEmployee("Luna",      "Magno",      LocalDate.of(2001, 2, 9),   hr, "35000");
        Employee emp38 = createEmployee("Francis",   "Reyes",      LocalDate.of(1994, 9, 3),   hr, "52000");
        Employee emp39 = createEmployee("Andrea",    "Cruz",       LocalDate.of(1987, 4, 17),  hr, "64000");
        Employee emp40 = createEmployee("Kenneth",   "Tan",        LocalDate.of(2002, 7, 28),  hr, "36000");
        Employee emp41 = createEmployee("Maricel",   "Lim",        LocalDate.of(1983, 1, 11),  hr, "70000");

        // Finance & Accounting (9 employees) — ages 24–52, salaries 50k–110k
        Employee emp42 = createEmployee("Jack",      "Fernandez",  LocalDate.of(1988, 10, 1),  finance, "85000");
        Employee emp43 = createEmployee("Chloe",     "Mercado",    LocalDate.of(1993, 4, 26),  finance, "75000");
        Employee emp44 = createEmployee("Sebastian", "Soriano",    LocalDate.of(1972, 7, 15),  finance, "108000");
        Employee emp45 = createEmployee("Penelope",  "Tolentino",  LocalDate.of(2000, 9, 8),   finance, "52000");
        Employee emp46 = createEmployee("Owen",      "Salazar",    LocalDate.of(1980, 1, 24),  finance, "95000");
        Employee emp47 = createEmployee("Beatrice",  "Flores",     LocalDate.of(1997, 6, 11),  finance, "63000");
        Employee emp48 = createEmployee("Raymond",   "Santos",     LocalDate.of(1985, 3, 29),  finance, "88000");
        Employee emp49 = createEmployee("Jasmine",   "Dela Rosa",  LocalDate.of(2001, 11, 14), finance, "50000");
        Employee emp50 = createEmployee("Rodrigo",   "Villanueva", LocalDate.of(1976, 8, 6),   finance, "110000");

        employeeRepository.saveAll(List.of(
                emp1,  emp2,  emp3,  emp4,  emp5,  emp6,  emp7,  emp8,  emp9,  emp10,
                emp11, emp12, emp13, emp14, emp15, emp16, emp17, emp18, emp19, emp20,
                emp21, emp22, emp23, emp24, emp25, emp26, emp27, emp28, emp29, emp30,
                emp31, emp32, emp33, emp34, emp35, emp36, emp37, emp38, emp39, emp40,
                emp41, emp42, emp43, emp44, emp45, emp46, emp47, emp48, emp49, emp50
        ));

        return "Database reseeded successfully — 2 users, 5 departments, 50 employees created.";
    }

    private Employee createEmployee(String firstName, String lastName,
                                    LocalDate dob, Department department,
                                    String salary) {
        Employee emp = new Employee();
        emp.setEmployeeId(generateEmployeeId());
        emp.setFirstName(firstName);
        emp.setLastName(lastName);
        emp.setDateOfBirth(dob);
        emp.setDepartment(department);
        emp.setSalary(new BigDecimal(salary));
        emp.setActive(true);
        return emp;
    }

    private Long generateEmployeeId() {
        Long employeeId;
        do {
            employeeId = (long) (Math.random() * 900000) + 100000;
        } while (employeeRepository.findByEmployeeId(employeeId).isPresent());
        return employeeId;
    }
}
