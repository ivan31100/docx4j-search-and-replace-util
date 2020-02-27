package de.phip1611;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.docx4j.wml.Text;
import org.junit.Assert;
import org.junit.Test;

public class Docx4JSRUtilTest {

    @Test
    public void testBuildMetaItemList() {
        List<Text> texts = asTexts("Hallo ${", "FOO", "} Bar");

        List<Docx4JSRUtil.TextMetaItem> list = Docx4JSRUtil.buildMetaItemList(texts);
        Assert.assertEquals(texts.get(0), list.get(0).getText());
        Assert.assertEquals(0, list.get(0).getPosition());
        Assert.assertEquals(texts.get(1), list.get(1).getText());
        Assert.assertEquals(1, list.get(1).getPosition());
        Assert.assertEquals(texts.get(2), list.get(2).getText());
        Assert.assertEquals(2, list.get(2).getPosition());

        Assert.assertEquals(0, list.get(0).getStart());
        Assert.assertEquals(7, list.get(0).getEnd());

        Assert.assertEquals(8, list.get(1).getStart());
        Assert.assertEquals(10, list.get(1).getEnd());

        Assert.assertEquals(11, list.get(2).getStart());
        Assert.assertEquals(15, list.get(2).getEnd());

        // noch testen von getPositionInsideTextObject()
        Assert.assertEquals(0, list.get(0).getPositionInsideTextObject(0));
        Assert.assertEquals(1, list.get(0).getPositionInsideTextObject(1));
        Assert.assertEquals(7, list.get(0).getPositionInsideTextObject(7));

        Assert.assertEquals(0, list.get(1).getPositionInsideTextObject(8));
        Assert.assertEquals(1, list.get(1).getPositionInsideTextObject(9));
        Assert.assertEquals(2, list.get(1).getPositionInsideTextObject(10));

        Assert.assertEquals(0, list.get(2).getPositionInsideTextObject(11));
        Assert.assertEquals(1, list.get(2).getPositionInsideTextObject(12));
        Assert.assertEquals(4, list.get(2).getPositionInsideTextObject(15));
    }

    @Test
    public void testBuildStringIndicesToTextMetaItemMap() {
        List<Text> texts = asTexts("Hallo ${", "FOO", "} Bar");

        List<Docx4JSRUtil.TextMetaItem> list = Docx4JSRUtil.buildMetaItemList(texts);
        Docx4JSRUtil.TextMetaItem[] array = Docx4JSRUtil.buildIndexToTextMetaItemArray(list);

        Assert.assertEquals(texts.get(0), array[0].getText());
        Assert.assertEquals(texts.get(0), array[3].getText());
        Assert.assertEquals(texts.get(0), array[7].getText());

        Assert.assertEquals(texts.get(1), array[8].getText());
        Assert.assertEquals(texts.get(1), array[9].getText());
        Assert.assertEquals(texts.get(1), array[10].getText());

        Assert.assertEquals(texts.get(2), array[11].getText());
        Assert.assertEquals(texts.get(2), array[14].getText());
        Assert.assertEquals(texts.get(2), array[15].getText());
        Assert.assertEquals(16, array.length);
    }

    @Test
    public void testBuildReplaceCommandsForOnePlaceholder() {
        Map<String, String> replaceLookupMap = new HashMap<>();
        replaceLookupMap.put("${FOO}", "Neuer Wert");

        String completeString = Docx4JSRUtil.getCompleteString(asTexts("Hallo ${", "FOO", "} Bar"));

        List<Docx4JSRUtil.ReplaceCommand> replaceCommands = Docx4JSRUtil.buildAllReplaceCommands(completeString, replaceLookupMap);
        Assert.assertEquals(1, replaceCommands.size());
        Assert.assertEquals("Neuer Wert", replaceCommands.get(0).getNewValue());
    }

    @Test
    public void testExecuteReplaceCommand() {
        Map<String, String> replaceLookupMap = new HashMap<>();
        replaceLookupMap.put("${FOO}", "FOO");
        replaceLookupMap.put("${BAR}", "BAR");;
        replaceLookupMap.put("${_}", "");

        List<Text> texts = asTexts("Hallo ${", "FOO", "} Bar$", "{BAR}${_}${BAR}");
        String completeString = Docx4JSRUtil.getCompleteString(texts);

        List<Docx4JSRUtil.TextMetaItem> metaItemList = Docx4JSRUtil.buildMetaItemList(texts);
        Docx4JSRUtil.TextMetaItem[] lookupArray = Docx4JSRUtil.buildIndexToTextMetaItemArray(metaItemList);
        List<Docx4JSRUtil.ReplaceCommand> replaceCommands = Docx4JSRUtil.buildAllReplaceCommands(completeString, replaceLookupMap);

        replaceCommands.forEach(rc -> Docx4JSRUtil.executeReplaceCommand(texts, rc, lookupArray));

        String newCompleteString = texts.stream()
                .map(Text::getValue)
                .filter(Objects::nonNull)
                .reduce(String::concat)
                .get();

        Assert.assertEquals("Hallo FOO BarBARBAR", newCompleteString);
    }

    private List<Text> asTexts(String... texts) {
        return Arrays.stream(texts).map(this::asText).collect(toList());
    }

    private Text asText(String text) {
        Text obj = new Text();
        obj.setValue(text);
        return obj;
    }

}