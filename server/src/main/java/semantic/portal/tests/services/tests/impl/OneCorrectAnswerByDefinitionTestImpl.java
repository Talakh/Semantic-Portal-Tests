package semantic.portal.tests.services.tests.impl;

import org.springframework.stereotype.Service;
import semantic.portal.tests.dto.ConceptDto;
import semantic.portal.tests.dto.ThesisDTO;
import semantic.portal.tests.model.Answer;
import semantic.portal.tests.model.DomainUrl;
import semantic.portal.tests.model.Test;
import semantic.portal.tests.model.TestDifficultLevel;
import semantic.portal.tests.services.api.ConceptApiService;
import semantic.portal.tests.services.tests.SPTest;

import java.util.*;

import static semantic.portal.tests.enums.TestTypeEnum.ONE_CORRECT_ANSWER;
import static semantic.portal.tests.utils.TestUtils.filterPossibleConcepts;
import static semantic.portal.tests.utils.TestUtils.getRandomConcept;

@Service
public class OneCorrectAnswerByDefinitionTestImpl implements SPTest {
    private static final String QUESTION_TEMPLATE = "The purpose of which concept describes the statement \"%s\"?";
    private final ConceptApiService conceptApiService;

    public OneCorrectAnswerByDefinitionTestImpl(ConceptApiService conceptApiService) {
        this.conceptApiService = conceptApiService;
    }

    @Override
    public Test create(List<ConceptDto> concepts,
                       List<ThesisDTO> theses,
                       List<String> thesesTypesForAnswer) {
        Map<Integer, ConceptDto> possibleConceptsForTest =
                filterPossibleConcepts(thesesTypesForAnswer, concepts, theses);
        ConceptDto question = getRandomConcept(possibleConceptsForTest);
        ThesisDTO thesisDTO = getConceptThesis(question.getId(), theses, thesesTypesForAnswer);
        return Test.builder()
                .domainUrl(Collections.singleton(question.getDomain()))
                .domainName(Collections.singleton(question.getConcept()))
                .question(String.format(QUESTION_TEMPLATE, thesisDTO.getThesis()))
                .answers(createAnswers(new ArrayList<>(possibleConceptsForTest.values()), thesisDTO))
                .type(ONE_CORRECT_ANSWER)
                .build();
    }

    @Override
    public boolean isEnoughTheses(List<ConceptDto> concepts,
                                  List<ThesisDTO> theses,
                                  TestDifficultLevel level) {
        if (level.getOneAnswerByDefinitionCount() <= 0) {
            return true;
        } else {
            Map<Integer, ConceptDto> possibleConceptsForTest =
                    filterPossibleConcepts(level.getOneAnswerByDefinitionThesisTypes(), concepts, theses);
            return possibleConceptsForTest.size() >= 4;
        }
    }

    private ThesisDTO getConceptThesis(int conceptId, List<ThesisDTO> theses, List<String> thesesTypesForAnswer) {
        return theses.stream()
                .filter(thesis -> thesis.getConceptId() == conceptId)
                .filter(thesis -> thesesTypesForAnswer.contains(thesis.getClazz()))
                .findAny()
                .orElseThrow(NullPointerException::new);
    }

    private List<Answer> createAnswers(List<ConceptDto> concepts, ThesisDTO thesisDTO) {
        ConceptDto rightConceptDto = conceptApiService.getConceptById(thesisDTO.getConceptId());
        List<Answer> answers = new ArrayList<>();
        Answer rightAnswer = Answer.createAnswer(rightConceptDto.getConcept(), true);
        answers.add(rightAnswer);
        Collections.shuffle(concepts);
        concepts.stream()
                .map(conceptDto -> Answer.createAnswer(conceptDto.getConcept(), false))
                .filter(answer -> !Objects.equals(rightAnswer.getAnswer(), answer.getAnswer()))
                .limit(4)
                .forEach(answers::add);
        Collections.shuffle(answers);
        return answers;
    }
}
