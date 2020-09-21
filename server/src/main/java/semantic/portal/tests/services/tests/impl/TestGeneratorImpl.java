package semantic.portal.tests.services.tests.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import semantic.portal.tests.dto.ConceptDto;
import semantic.portal.tests.dto.ThesisDTO;
import semantic.portal.tests.enums.DifficultLevelEnum;
import semantic.portal.tests.model.Test;
import semantic.portal.tests.services.tests.SPTest;
import semantic.portal.tests.services.tests.TestGenerator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TestGeneratorImpl implements TestGenerator {
    private final SPTest severalCorrectAnswersTest;
    private final SPTest oneCorrectAnswerTest;
    private final SPTest matchTest;

    public TestGeneratorImpl(@Qualifier("severalCorrectAnswersTestImpl") SPTest severalCorrectAnswersTest,
                             @Qualifier("oneCorrectAnswerTestImpl") SPTest oneCorrectAnswerTest,
                             @Qualifier("matchTestImpl") SPTest matchTest) {
        this.severalCorrectAnswersTest = severalCorrectAnswersTest;
        this.oneCorrectAnswerTest = oneCorrectAnswerTest;
        this.matchTest = matchTest;
    }

    @Override
    public List<Test> generate(List<ConceptDto> concepts, List<ThesisDTO> theses, DifficultLevelEnum difficult) {
        List<Test> tests = new ArrayList<>();
        for(int i = 0; i < 300; i++) {
            tests.add(oneCorrectAnswerTest.create(concepts, theses));
        }
        for (int i = 0; i < 10000; i++) {
            tests.add(severalCorrectAnswersTest.create(concepts, theses));
        }
        return tests;
    }
}
