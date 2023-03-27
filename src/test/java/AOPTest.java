import com.rhys.spring.DI.BeanReference;
import com.rhys.spring.IoC.BeanPostProcessor;
import com.rhys.spring.IoC.GenericBeanDefinition;
import com.rhys.spring.IoC.PreBuildBeanFactory;
import com.rhys.spring.IoC.exception.BeanDefinitionRegistryException;
import com.rhys.spring.aop.advisor.AspectJPointCutAdvisor;
import com.rhys.spring.aop.impl.MyAfterReturningAdvice;
import com.rhys.spring.aop.impl.MyBeforeAdvice;
import com.rhys.spring.aop.impl.MyMethodInterceptor;
import com.rhys.spring.aop.pointcut.AspectJExpressionPointCut;
import com.rhys.spring.aop.weaving.AdvisorAutoProxyCreator;
import com.rhys.spring.demo.TestA;
import com.rhys.spring.demo.TestB;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 3:34 AM
 */
public class AOPTest {
    static PreBuildBeanFactory beanFactory = new PreBuildBeanFactory();

    @Test
    public void testAop() throws Exception {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

        //配置TestABean
        beanDefinition.setBeanClass(TestA.class);
        List<Object> args = new ArrayList<>();
        args.add("testABean");
        args.add(new BeanReference("bBean"));
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aBean", beanDefinition);

        //配置TestBBean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(TestB.class);
        args = new ArrayList<>();
        args.add("testBBean");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("bBean", beanDefinition);

        //前置增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyBeforeAdvice.class);
        beanFactory.registryBeanDefinition("myBeforeAdvice", beanDefinition);

        //后置增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyAfterReturningAdvice.class);
        beanFactory.registryBeanDefinition("myAfterReturningAdvice", beanDefinition);

        //环绕增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyMethodInterceptor.class);
        beanFactory.registryBeanDefinition("myMethodInterceptor", beanDefinition);

        //注册Advisor（通知者/切面）bean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myBeforeAdvice");
        //对TestA类中的以test为前缀的所有方法进行前置增强
        args.add("excution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor1", beanDefinition);


        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myAfterReturningAdvice");
        //对TestA类中的以test为前缀的所有方法进行后置增强
        args.add("excution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor2", beanDefinition);


        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myMethodInterceptor");
        //对TestA类中的以test为前缀的所有方法进行环绕增强
        args.add("excution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor3", beanDefinition);

        //配置AdvisorAutoProxyCreator的Bean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AdvisorAutoProxyCreator.class);
        beanFactory.registryBeanDefinition("advisorAutoProxyCreator", beanDefinition);

        //生成Type映射
        beanFactory.registerTypeMap();

        //注册完所需要的BeanDefinition后，且在生成普通Bean实例前，从BeanFactory中获得用户配置的所有BeanPostProcessor类型的Bean实例，注册到BeanFactory
        List<BeanPostProcessor> beanPostProcessorList = beanFactory.getBeanListOfType(BeanPostProcessor.class);
        if (!beanPostProcessorList.isEmpty()) {
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanFactory.registerBeanPostProcessor(beanPostProcessor);
            }
        }

        //提前实例化Bean
        beanFactory.instantiateSingleton();

        TestA testA = (TestA) beanFactory.getBean("aBean");
        testA.execute();
        testA.testInit();
        testA.testDestroy();
    }
}
