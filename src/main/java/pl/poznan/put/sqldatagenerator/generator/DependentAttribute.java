package pl.poznan.put.sqldatagenerator.generator;

@Deprecated
public class DependentAttribute extends Attribute {

    private final Attribute baseAttribute;

    public DependentAttribute(Attribute attribute, Attribute baseAttribute) {
        super(attribute);
        if (attribute.getType() != baseAttribute.getType()) {
            throw new RuntimeException("Dependent attribute must have the same type as base attribute");
        }
        this.baseAttribute = baseAttribute;
    }

    @Override
    public void clear() {
    }

    @Override
    public String getValue() {
        return baseAttribute.getValue();
    }

    @Override
    public void setValue(String value) {
        throw new RuntimeException("Attempt to set value of dependent attribute");
    }
}
