/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2007 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.classfile.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;

import edu.umd.cs.findbugs.ba.ClassMember;
import edu.umd.cs.findbugs.ba.SignatureParser;
import edu.umd.cs.findbugs.ba.XFactory;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.FieldDescriptor;
import edu.umd.cs.findbugs.classfile.IClassConstants;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.util.Util;

/**
 * @author pugh
 */
public class FieldInfo extends FieldDescriptor implements XField, AnnotatedObject {

	static public class Builder {
		final int accessFlags;

		final String className, fieldName, fieldSignature;

		String fieldSourceSignature;

		final Map<ClassDescriptor, AnnotationValue> fieldAnnotations = new HashMap<ClassDescriptor, AnnotationValue>();

		final Map<Integer, Map<ClassDescriptor, AnnotationValue>> fieldParameterAnnotations = new HashMap<Integer, Map<ClassDescriptor, AnnotationValue>>();

		public Builder(@DottedClassName String className, String fieldName, String fieldSignature, int accessFlags) {
			this.className = className;
			this.fieldName = fieldName;
			this.fieldSignature = fieldSignature;
			this.accessFlags = accessFlags;
		}

		public void setSourceSignature(String fieldSourceSignature) {
			this.fieldSourceSignature = fieldSourceSignature;
		}

		public void addAnnotation(String name, AnnotationValue value) {
			ClassDescriptor annotationClass = ClassDescriptor.createClassDescriptorFromSignature(name);
			fieldAnnotations.put(annotationClass, value);
		}

		public FieldInfo build() {
			return new FieldInfo(className, fieldName, fieldSignature, fieldSourceSignature, accessFlags, fieldAnnotations, 
				 fieldParameterAnnotations, true);
		}
	}

	final int accessFlags;

	final String fieldSourceSignature;
	final Map<ClassDescriptor, AnnotationValue> fieldAnnotations;

	final Map<Integer, Map<ClassDescriptor, AnnotationValue>> fieldParameterAnnotations;
	final boolean isResolved;
	
	/**
     * @param className
     * @param fieldName
     * @param fieldSignature
     * @param isStatic
     * @param accessFlags
     * @param fieldAnnotations
     * @param fieldParameterAnnotations
     * @param isResolved
     */
    private FieldInfo(
    		String className,
    		String fieldName,
    		String fieldSignature,
    		String fieldSourceSignature,
    		int accessFlags,
    		Map<ClassDescriptor, AnnotationValue> fieldAnnotations,
    		Map<Integer,
    		Map<ClassDescriptor, AnnotationValue>> fieldParameterAnnotations,
    		boolean isResolved) {
	    super(className, fieldName, fieldSignature, (accessFlags & Constants.ACC_STATIC) != 0);
	    this.accessFlags = accessFlags | (fieldName.startsWith("this$") ? Constants.ACC_FINAL : 0);
		this.fieldSourceSignature = fieldSourceSignature;
		this.fieldAnnotations = Util.immutableMap(fieldAnnotations);
		this.fieldParameterAnnotations = Util.immutableMap(fieldParameterAnnotations);
		this.isResolved = isResolved;
    }


    public int getNumParams() {
    	return new SignatureParser(getSignature()).getNumParameters();
    }

    private boolean checkFlag(int flag) {
    	return (accessFlags & flag) != 0;
    }

    public boolean isNative() {
	    return checkFlag(Constants.ACC_NATIVE);
    }


    public boolean isSynchronized() {
    	return checkFlag(Constants.ACC_SYNCHRONIZED);
    }

    public @DottedClassName String getClassName() {
	    return getClassDescriptor().toDottedClassName();
    }

    public @DottedClassName String getPackageName() {
	    return  getClassDescriptor().getPackageName();
    }
	public String getSourceSignature() {
		return fieldSourceSignature;
	}

	/* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object rhs) {
    	if (rhs instanceof FieldDescriptor) {
    		return super.compareTo((FieldDescriptor) rhs);
    	}
    	
    	if (rhs instanceof XField) {
    		return XFactory.compare((XField) this, (XField) rhs); 
    	}
    	
    	throw new ClassCastException("Can't compare a " + this.getClass().getName() + " to a " + rhs.getClass().getName());
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#getAccessFlags()
     */
    public int getAccessFlags() {
	    return accessFlags;
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#isFinal()
     */
    public boolean isFinal() {
	    return checkFlag(Constants.ACC_FINAL);
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#isPrivate()
     */
    public boolean isPrivate() {
    	return checkFlag(Constants.ACC_PRIVATE);
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#isProtected()
     */
    public boolean isProtected() {
    	return checkFlag(Constants.ACC_PROTECTED);
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#isPublic()
     */
    public boolean isPublic() {
    	return checkFlag(Constants.ACC_PUBLIC);
    }

	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.AccessibleEntity#isResolved()
     */
    public boolean isResolved() {
	    return this.isResolved;
    }


	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.XField#isReferenceType()
     */
    public boolean isReferenceType() {
		return getSignature().startsWith("L") || getSignature().startsWith("[");
	}


	/* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.XField#isVolatile()
     */
    public boolean isVolatile() {
	    return checkFlag(Constants.ACC_VOLATILE);
    }

    public Collection<ClassDescriptor> getAnnotationDescriptors() {
		return fieldAnnotations.keySet();
	}
	public AnnotationValue getAnnotation(ClassDescriptor desc) {
		return fieldAnnotations.get(desc);
	}
	public Collection<AnnotationValue> getAnnotations() {
		return fieldAnnotations.values();
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.ba.XField#getFieldDescriptor()
	 */
	public FieldDescriptor getFieldDescriptor() {
		return this;
	}
	
	public static FieldInfo createUnresolvedFieldInfo(String className, String name, String signature, boolean isStatic) {
		return new FieldInfo(
				className,
				name,
				signature,
				"", // XXX
				isStatic ? Constants.ACC_STATIC : 0,
				new HashMap<ClassDescriptor, AnnotationValue>(),
				new HashMap<Integer, Map<ClassDescriptor,AnnotationValue>>(),
				false);
	}
}
