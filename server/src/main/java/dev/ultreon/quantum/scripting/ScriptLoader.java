//package dev.ultreon.quantum.scripting;
//
//import dev.ultreon.quantum.CommonConstants;
//import dev.ultreon.quantum.CommonLoader;
//import dev.ultreon.quantum.GamePlatform;
//import dev.ultreon.quantum.platform.PlatformFeature;
//import dev.ultreon.quantum.resources.Resource;
//import dev.ultreon.quantum.resources.ResourceCategory;
//import dev.ultreon.quantum.resources.ResourceManager;
//import dev.ultreon.quantum.util.NamespaceID;
//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.ErrorReporter;
//import org.mozilla.javascript.EvaluatorException;
//import org.mozilla.javascript.PolicySecurityController;
//
//import java.io.Closeable;
//import java.io.IOException;
//import java.util.List;
//import java.util.TimeZone;
//
//public class ScriptLoader implements Closeable, ErrorReporter {
//    private final Context context;
//
//    public ScriptLoader() {
//        context = Context.enter();
//    }
//
//    public Context getContext() {
//        return context;
//    }
//
//    public void init() {
//        context.initStandardObjects();
//        context.setInterpretedMode(GamePlatform.get().isFeatureSupported(PlatformFeature.JsBytecode));
//        context.setLanguageVersion(Context.VERSION_ECMASCRIPT);
//        context.setTimeZone(TimeZone.getDefault());
//        context.setErrorReporter(this);
//        context.setGeneratingDebug(true);
//        context.setGeneratingSource(true);
//        context.setClassShutter(new ScriptClassShutter());
//        context.setApplicationClassLoader(ScriptLoader.class.getClassLoader());
//        context.setInstructionObserverThreshold(10000);
//        context.setSecurityController(new PolicySecurityController());
//    }
//
//    public void evaluate(String script) {
//        try {
//            context.evaluateString(context.initStandardObjects(), script, "<script>", 1, null);
//        } catch (EvaluatorException e) {
//            error(e.getMessage(), "<script>", e.lineNumber(), e.lineSource(), e.columnNumber());
//        }
//    }
//
//    public void evaluate(String script, String filename) {
//        try {
//            context.evaluateString(context.initStandardObjects(), script, filename, 1, null);
//        } catch (EvaluatorException e) {
//            error(e.getMessage(), filename, e.lineNumber(), e.lineSource(), e.columnNumber());
//        }
//    }
//
//    public void evaluate(String script, String filename, int line) {
//        try {
//            context.evaluateString(context.initStandardObjects(), script, filename, line, null);
//        } catch (EvaluatorException e) {
//            error(e.getMessage(), filename, e.lineNumber(), e.lineSource(), e.columnNumber());
//        }
//    }
//
//    public void evaluate(NamespaceID location) {
//        try {
//            ResourceManager contentResources = CommonLoader.get().getContentResources();
//            Resource resource = contentResources.getResource(location);
//            if (resource == null) {
//                error("Failed to load script " + location.toString() + ": Resource not found", location.toString(), 1, null, 0);
//                return;
//            }
//            context.evaluateReader(context.initStandardObjects(), resource.openReader(), location.toString(), 1, null);
//        } catch (EvaluatorException e) {
//            error(e.getMessage(), location.toString(), e.lineNumber(), e.lineSource(), e.columnNumber());
//        } catch (IOException e) {
//            error("Failed to load script " + location.toString() + ": " + e.getMessage(), location.toString(), 1, null, 0);
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//        Context.exit();
//    }
//
//    @Override
//    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
//        CommonConstants.LOGGER.warn(message + " at " + sourceName + ":" + line + ":" + lineOffset + "\n" + lineSource + "\n" + " ".repeat(lineOffset) + "^");
//    }
//
//    @Override
//    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
//        CommonConstants.LOGGER.error(message + " at " + sourceName + ":" + line + ":" + lineOffset + "\n" + lineSource + "\n" + " ".repeat(lineOffset) + "^");
//    }
//
//    @Override
//    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
//        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
//    }
//
//    public void reload(ResourceManager resources) {
//        List<ResourceCategory> categories = resources.getResourceCategory("scripts");
//        for (ResourceCategory category : categories) {
//            category.forEach((namespaceID, staticResource) -> {
//                if (namespaceID.getPath().equals("scripts/index.js")) {
//                    try {
//                        context.evaluateReader(context.initStandardObjects(), staticResource.openReader(), namespaceID.toString(), 1, null);
//                    } catch (Exception e) {
//                        error("Failed to load script " + namespaceID + ": " + e.getMessage(), namespaceID.toString(), 1, null, 0);
//                    }
//                }
//            });
//        }
//    }
//}
