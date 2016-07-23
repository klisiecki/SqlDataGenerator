package pl.poznan.put.sqldatagenerator.restriction;

import com.google.common.collect.HashMultimap;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RestrictionsByAttribute {
    private HashMultimap<Attribute, Restriction> restrictionsByAttribute = HashMultimap.create();

    public void put(Attribute attribute, Restriction restriction) {
        restrictionsByAttribute.put(attribute, restriction);
    }

    public boolean remove(Attribute attribute, Restriction restriction) {
        return restrictionsByAttribute.remove(attribute, restriction);
    }

    public Set<Map.Entry<Attribute, Restriction>> entries() {
        return restrictionsByAttribute.entries();
    }

    public Set<Map.Entry<Attribute, Collection<Restriction>>> groupedEntries() {
        return restrictionsByAttribute.asMap().entrySet();
    }

    public Collection<Restriction> values() {
        return restrictionsByAttribute.values();
    }

}
