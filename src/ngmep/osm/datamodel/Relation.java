package ngmep.osm.datamodel;

/*
	ngmep2osm - importador de datos de ngmep a openstreetmap
	
	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.List;


public class Relation extends Entity{

    private List<RelationMember> members = new ArrayList<RelationMember>();
    public Relation () {
        super();
    }
    public  List<RelationMember> getMembers() {
        return this.members;
    }
    public void addMember(RelationMember member){
        if (!this.members.contains(member)){
            this.members.add(member);
            setModified(true);
        }
        
    }
    public boolean containsMemberWithRole(String role) {
        for (RelationMember member: members) {
            if (role.equals(member.getRole())){
                return true;
            }
        }
        return false;
    }
}

