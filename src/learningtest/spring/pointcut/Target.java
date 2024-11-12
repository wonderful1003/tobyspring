package learningtest.spring.pointcut;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public class Target implements TargetInterface{
	public void hello() {}
	public void hello(String a) {}
	public int minus(int a, int b) throws RuntimeException {return 0;}
	public int plus(int a, int b) {return 0;}
	public void method() {}
	
	@Test
	public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException{
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		
		pointcut.setExpression("execution(public int " + 
				"learningtest.spring.pointcut.Target.minus(int,int) "+
				"throws java.lang.RuntimeException)");
		
		//Target.minus()
		assertThat(pointcut.getClassFilter().matches(Target.class) && 
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("minus", int.class, int.class), null), is(true));
		
		//Target.plus()
		assertThat(pointcut.getClassFilter().matches(Target.class) && 
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("plus", int.class, int.class), null), is(false));
		
		//Bean.method()
		assertThat(pointcut.getClassFilter().matches(Bean.class) && 
				pointcut.getMethodMatcher().matches(
						Target.class.getMethod("method"), null), is(false));
	}
}