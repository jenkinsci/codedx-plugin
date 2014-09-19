package org.jenkinsci.plugins.codedx;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

public class PathEntry extends AbstractDescribableImpl<PathEntry> implements Serializable{

	private final String value;

	@DataBoundConstructor
	public PathEntry(final String value){
		
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
    @Extension
    public static class DescriptorImpl extends Descriptor<PathEntry> {
        public String getDisplayName() { return ""; }
    }
}
