package com.rhys.testSourceCode.config.xml;

import org.springframework.beans.factory.annotation.Autowired;

public class BeanF {

	@Autowired
	private BeanE be;

	public void do1() {
		System.out.println("----------" + this + " do1");
		this.be.doSomething();
	}
}
