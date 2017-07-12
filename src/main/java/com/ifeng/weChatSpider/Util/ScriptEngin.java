package com.ifeng.weChatSpider.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;

public class ScriptEngin {
	
	private Map<String,Object> scopes = new HashMap<String,Object>();
	private static ScriptEngin instance =null;
	
	public static ScriptEngin getInstance()
	{
		if(instance==null)
		{
			instance = new ScriptEngin();
		}
		return instance;
	}
	private ScriptEngin()
	{
		
	}
	public void run(String script,Map<String,Object> context) throws Exception
	{
		Context cx = Context.enter();
		try
		{			
			Scriptable scope = cx.initStandardObjects();
			Object jsout = cx.javaToJS(System.out, scope);
			ScriptableObject.putProperty(scope, "out", jsout);
			Object jscontext = cx.javaToJS(context, scope);
			ScriptableObject.putProperty(scope, "context", jscontext);
			Object result = cx.evaluateString(scope, script, null, 0, null);

		}
		catch (org.mozilla.javascript.RhinoException e) {
			throw new Exception("运行脚本出错：第"+(e.lineNumber()+1)+"行:"+e.getMessage());
			
		}
		finally
		{
			cx.exit();
		}
	}
	public void run(String script,String param) throws Exception
	{
		Context cx = Context.enter();
		try
		{
			Scriptable scope = cx.initStandardObjects();
			Object jscontext = cx.javaToJS(param, scope);
			ScriptableObject.putProperty(scope, param, jscontext);
			Object result = cx.evaluateString(scope, script, null, 0, null);

		}
		catch (org.mozilla.javascript.RhinoException e) {
			throw new Exception("运行脚本出错：第"+(e.lineNumber()+1)+"行:"+e.getMessage());

		}
		finally
		{
			cx.exit();
		}
	}
	public String runScript(String js, String functionName, Object[] functionParams) {
		Context rhino = Context.enter();
		rhino.setOptimizationLevel(-1);
		try {
			Scriptable scope = rhino.initStandardObjects();
			rhino.evaluateString(scope, js, null, 1, null);
			Function function = (Function) scope.get(functionName, scope);
			Object result = function.call(rhino, scope, scope, functionParams);
			return result.toString();//(String) function.call(rhino, scope, scope, functionParams);
		} finally {
			Context.exit();
		}
	}
}

