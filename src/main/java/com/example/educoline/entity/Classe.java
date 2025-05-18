package com.example.educoline.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "classes")
public class Classe {

    @Version
    private int version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String level;

    @ManyToMany
    @JoinTable(
            name = "classe_enseignant",
            joinColumns = @JoinColumn(name = "classe_id"),
            inverseJoinColumns = @JoinColumn(name = "enseignant_id")
    )
    private Set<Enseignant> enseignants = new HashSet<>();

    public Classe() {}

    public Classe(String name, String level) {
        this.name = name;
        this.level = level;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Set<Enseignant> getEnseignants() { return enseignants; }
    public void setEnseignants(Set<Enseignant> enseignants) { this.enseignants = enseignants; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public void addEnseignant(Enseignant enseignant) {
        this.enseignants.add(enseignant);
        enseignant.getClasses().add(this);
    }

    public void removeEnseignant(Enseignant enseignant) {
        this.enseignants.remove(enseignant);
        enseignant.getClasses().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classe)) return false;
        Classe classe = (Classe) o;
        return Objects.equals(id, classe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    List<Classe> findByEnseignant_Id(Long enseignantId) {
        return null;
    }


    @Override
    public String toString() {
        return "Classe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level='" + level + '\'' +
                ", enseignants=" + enseignants.size() +
                '}';
    }
}
