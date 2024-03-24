package com.windsome.service.product;

import com.windsome.entity.Color;
import com.windsome.repository.product.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ColorService {

    private final ColorRepository colorRepository;

    public Color getColorByColorId(Long colorId) {
        return colorRepository.findById(colorId).orElseThrow(EntityNotFoundException::new);
    }

    public List<Color> getColors() {
        return colorRepository.findAll();
    }
}
