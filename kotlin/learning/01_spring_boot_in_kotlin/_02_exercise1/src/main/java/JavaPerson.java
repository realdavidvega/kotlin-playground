package main.java;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class JavaPerson {

    private final Long id;
    private final String title;
    private final String firstName;
    private final String surname;
    private final Calendar dateOfBirth;

    public JavaPerson(Long id, String title, String firstName, String surname, Calendar dateOfBirth) {
        if (id == null || title == null || firstName == null || surname == null)
            throw new NullPointerException();
        this.id = id;
        this.title= title;
        this.firstName = firstName;
        this.surname = surname;
        this.dateOfBirth =dateOfBirth;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String toString() {
        return title + " " + firstName + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaPerson that = (JavaPerson) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(surname, that.surname);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, title, firstName, surname);
    }

    public static int getAge(Calendar dateOfBirth) {
        if (dateOfBirth == null) return -1;

        Calendar today = new GregorianCalendar();
        int years = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
        if (dateOfBirth.get(Calendar.DAY_OF_YEAR) >= today.get(Calendar.YEAR)) {
            return years -1;
        }
        else {
            return years;
        }

    }

    public int getAge() {
        return getAge(dateOfBirth);
    }
}
