package com.example.educoline.service;

import com.example.educoline.entity.Classe;
import com.example.educoline.repository.ClasseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClasseService {

    private final ClasseRepository classeRepository;

    @Autowired
    public ClasseService(ClasseRepository classeRepository) {
        this.classeRepository = classeRepository;
    }



    // Vous pouvez ajouter d'autres méthodes CRUD si nécessaire
    public List<Classe> getAllClasses() {
        return classeRepository.findAll();
    }

    public Classe saveClasse(Classe classe) {
        return classeRepository.save(classe);
    }

    public Optional<Classe> getClasseById(Long id) {
        return classeRepository.findById(id);
    }

    public void deleteClasse(Long id) {
        classeRepository.deleteById(id);
    }
}
