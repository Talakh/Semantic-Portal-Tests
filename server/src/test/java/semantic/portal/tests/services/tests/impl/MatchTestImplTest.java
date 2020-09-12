package semantic.portal.tests.services.tests.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import semantic.portal.tests.model.Answer;
import semantic.portal.tests.services.api.impl.BranchApiServiceImpl;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MatchTestImplTest {
    @Autowired
    private MatchTestImpl matchTest;
    @Autowired
    private BranchApiServiceImpl branchApiService;

    @Test
    public void createTest(){
        semantic.portal.tests.model.Test test = matchTest.create(
                branchApiService.getConcepts("java"), branchApiService.getTheses("java"));
        System.out.println("Q - " + test.getQuestion());
        System.out.println("A - " + test.getAnswers().stream().map(Answer::getAnswer).collect(Collectors.joining("\n")));
    }
}
