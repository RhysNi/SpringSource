package com.rhys.spring.demo;

import com.rhys.spring.context.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 12:21 AM
 */
@Component("testABean")
@Primary
public class TestA {
    private String name;
    private TestB testB;


    @Autowired
    public TestA(@Value("TestA_RhysNi") String name, @Qualifier("testBBean") TestB testB) {
        super();
        this.name = name;
        this.testB = testB;
        System.out.println("调用了含有testB参数的构造方法");
    }


    public TestA(TestB testB) {
        this.testB = testB;
    }

    public void execute() {
        System.out.println("aBean execute:" + this.name + "\n testB.name:" + this.testB.getName());
    }

    public void testInit() {
        System.out.println("aBean 执行了init()方法");
    }

    public void testDestroy() {
        System.out.println("aBean 执行了destroy()方法");
    }
}
