package de.lisaplus.atlas.codegen

import de.lisaplus.atlas.interf.ICodeGen
import de.lisaplus.atlas.model.Model
import de.lisaplus.atlas.model.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the base for code generators that only create one file
 * Created by eiko on 05.06.17.
 */
abstract class SingleFileGenarator extends GeneratorBase implements ICodeGen {
    /**
     * This funkction is called to start the code generation process
     * @param model model that is the base for the code generation
     * @param outputBasePath under this path the output is generated. A generator can add a needed sub path if needed (for instance for packeges)
     * @param extraParams additional parameters to initialize the generator
     */
    void doCodeGen(Model model, String outputBasePath, Map<String,String> extraParams) {
        if (!template) {
            def errorMsg = "template not initialized"
            getLogger().error(errorMsg)
            throw new Exception(errorMsg)
        }

        def data = createTemplateDataMap(model)
        this.extraParams = extraParams
        if (extraParams) {
            data.extraParam = extraParams
        }
        else {
            data.extraParam = [:]
        }
        initGeneratorScriptForTemplate(data)

        removeUnneededTypes(data,extraParams)
        def ergebnis = template.make(data)

        def destFileName = getDestFileName(model,extraParams)
        def destDir = getDestDir(model,outputBasePath,extraParams)

        def shouldRemoveEmptyLines = extraParams['removeEmptyLines']

        def resultString = shouldRemoveEmptyLines ? removeEmptyLines (ergebnis.toString()) :
                ergebnis.toString()
        File file=new File("${destDir}/${destFileName}")
        file.write( resultString )
    }

    private void removeUnneededTypes (Map data, Map<String,String> extraParams) {
        def blackListed=data.extraParam['blackListed']
        def whiteListed=data.extraParam['whiteListed']

        def neededAttrib = extraParams['containsAttrib']
        def missingAttrib = extraParams['missingAttrib']
        def neededTag = extraParams['neededTag']
        def neededTagList = splitValueToArray(neededTag)

        def ignoredTag = extraParams['ignoreTag']
        def ignoredTagList = splitValueToArray(ignoredTag)
        List<Type> neededTypes = []
        data.model.types*.each { type ->
            boolean handleNeeded = neededAttrib ? type.properties.find { prop ->
                return prop.name==neededAttrib
            } != null : true
            boolean handleMissing = missingAttrib ? type.properties.find { prop ->
                return prop.name==missingAttrib
            } == null : true

            boolean handleType=true;
            if (whiteListed && (!whiteListed.contains(type.name))) {
                handleType = false
                println "ingnored by white-list: ${type.name}"
            }
            else if (blackListed && blackListed.contains(type.name)) {
                handleType = false
                println "ingnored by black-list: ${type.name}"
            }

            boolean handleTag = ignoredTagList ? type.tags.find { tag ->
                return ignoredTagList.contains(tag)
            } == null : true

            if (handleTag && neededTagList) {
                boolean allTagsFound=true;
                neededTagList.each { needed ->
                    if (!type.tags.contains(needed)) {
                        allTagsFound = false
                    }
                }
                handleTag = allTagsFound
            }

            if (handleType && handleNeeded && handleMissing && handleTag) {
                neededTypes.add(type)
            }
        }
        data.model.types = neededTypes
    }

    private List<String> splitValueToArray(String value) {
        if (!value) return []
        return value.indexOf(',')!=-1 ? value.split(',') : value.split(':')
    }
}
